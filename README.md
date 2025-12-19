# 一.秒杀业务总结

## 	1.秒杀后端核心设计思路

​	秒杀业务的核心矛盾是高并发请求vs有限库存/资源，后端的设计主要目的是解决**（防超卖）、（扛高并发）、（防刷/防恶意请求）**，解决思路：
​		1、**数据库层**：mp操作
​		2、**缓存层**：redis+redisson
​				redis用于存储秒杀库存、活动状态、订单信息
​				redisson提供分布式信号量实现限流、解决高并发下的资源竞争问题
​		3、**消息队列**：rocketmq
​				处理支付回调后的异步业务（如订单状态更新、课程购买记录生成）、通过事务消息保证数据一致性。
​		4、**限流熔断**：Sentinel
​				全局异常处理器捕获限流/熔断异常。
​		5、**分布式业务**：基于rocketmq的事务消息

## 	2.核心业务逻辑实现

### 		2.1秒杀活动发布

​		**触发入口**：管理员调用`KillActivityController`的`publish/{actId}`接口，由`KillActivityServiceImpl`实现发布逻辑。

```java
// 1. 校验活动状态（未发布状态才能发布）
KillActivity act = this.getById(actId);
if(act.getPublishStatus() == PublishStatus.PUBLISH_SUC.getCode()) 
    throw new BusinessException("活动已发布");

// 2. 更新活动和关联课程的发布状态、发布时间
act.setPublishStatus(PublishStatus.PUBLISH_SUC.getCode());
act.setPublishTime(new Date());
this.updateById(act);
// 同步更新关联的秒杀课程状态
killCourseService.update(Wrappers.lambdaUpdate(KillCourse.class)
        .set(KillCourse::getPublishStatus, PublishStatus.PUBLISH_SUC.getCode())
        .eq(KillCourse::getActivityId, actId));

// 3. 缓存秒杀课程信息到Redis（预热缓存，减少秒杀时的DB压力）
List<KillCourse> killCourses = killCourseService.list(Wrappers.lambdaQuery(KillCourse.class)
        .eq(KillCourse::getActivityId, actId));
Map<String, KillCourse> courseMap = killCourses.stream()
        .collect(Collectors.toMap(KillCourse::getId, course -> course));
cacheService.hput(CacheKeys.KILL_ACTIVITY + actId, courseMap);
	
```

### 		2.2秒杀库存控制与并发处理

​	**核心接口：**`KillController`的`kill/{courseId}`接口，处理用户秒杀请求。
​	**技术亮点：**结合 Redisson 信号量限流 + Lua 脚本原子性操作，防止超卖和并发安全问题。

```java
@GetMapping("kill/{courseId}")
public String kill(@PathVariable String courseId) {
    // 1. Redisson信号量限流（控制并发请求数，如100ms内允许1000个请求）
    RSemaphore killSem = redissonClient.getSemaphore("killSem");
    boolean acquired = killSem.tryAcquire(); // 获取许可，未获取到则直接返回失败
    if (!acquired) return "kill fail";
    
    try {
        // 2. Lua脚本原子性检查并扣减库存
        String stockKey = "ymcc:kill:courseId:" + courseId;
        String lua = "local seckill_num = tonumber(ARGV[1]);" +
                    "local stock_key = KEYS[1];" +
                    "local current_stock = redis.pcall('GET', stock_key) or 0;" +
                    "current_stock = tonumber(current_stock);" +
                    "if current_stock < seckill_num then return -1; end;" + // 库存不足
                    "local remain_stock = redis.pcall('DECRBY', stock_key, seckill_num);" + // 扣减库存
                    "return remain_stock;";
        
        RedisScript<Long> script = RedisScript.of(lua, Long.class);
        Long remain = (Long) redisTemplate.execute(script, Arrays.asList(stockKey), 1);
        
        if (remain < 0) return "kill fail"; // 库存不足
        
        // 3. 生成秒杀订单（暂存Redis，后续异步落库）
        String orderKey = "ymcc:killOrder:" + courseId;
        redisTemplate.opsForList().leftPush(orderKey, IdUtil.getSnowflakeNextIdStr());
        return "kill suc";
    } finally {
        // 4. 释放信号量许可
        if (acquired) killSem.release();
    }
}
```

## 3.总结

​	后端秒杀业务通过 **“缓存预热（活动发布时加载数据到 Redis）→ 限流（Redisson 信号量）→ 原子操作（Lua 脚本扣减库存）→ 异步处理（RocketMQ 事务消息）”** 的流程，解决了高并发下的库存超卖、数据一致性、系统可用性问题。技术栈上融合了 Redis、Redisson、RocketMQ、Sentinel 等组件，形成完整的高并发解决方案。


# DAY12:面试回答

​	一、重点内容详解

#### 1. 秒杀全流程（结合代码细节）

秒杀流程可分为**前端触发→后端限流→库存扣减→订单生成→支付回调→订单确认**六大环节，具体如下：

1. **前端触发**：用户在秒杀详情页（kill-detail.html）点击 “立即秒杀” 按钮，前端携带courseId和killId调用后端/test/kill/{courseId}接口，同时通过倒计时组件（countdownsync）实时同步秒杀状态（未开始 / 进行中 / 已结束）。

1. **后端限流**：接口首先通过 Redisson 的RSemaphore（信号量killSem）控制并发请求数（如 100ms 内允许 1000 个请求），未获取许可的请求直接返回 “秒杀失败”。

1. **库存扣减**：通过 Lua 脚本原子性操作 Redis 库存（ymcc:kill:courseId:{courseId}），检查库存是否充足并扣减，避免超卖（脚本返回剩余库存，若 < 0 则失败）。

1. **订单生成**：扣减库存成功后，生成秒杀订单号（雪花算法IdUtil.getSnowflakeNextIdStr()）并暂存 Redis（ymcc:killOrder:{courseId}），随后前端跳转至订单确认页（order.confirm.html），携带type=1（标识秒杀订单）。

1. **支付处理**：用户确认订单后调用下单接口，订单服务（CourseOrderServiceImpl）生成正式订单并发送 RocketMQ 事务消息，确保订单创建与支付单生成的原子性。

1. **支付回调**：支付宝支付成功后，AliPayCallBackController接收回调，通过事务消息更新订单状态，并通知课程服务记录购买信息。

#### 2. MQ 事务消息（保证数据一致性）

项目通过 RocketMQ 事务消息解决分布式事务问题，核心场景包括**订单创建与支付单同步**、**支付结果与订单状态同步**，实现逻辑如下：

- **事务消息流程**：

1. 1. 发送方（如订单服务）调用sendMessageInTransaction发送半消息到 MQ；

1. 1. MQ 确认接收后，触发本地事务方法（executeLocalTransaction）；

1. 1. 本地事务成功则提交消息，失败则回滚；

1. 1. 若本地事务结果未知，MQ 会定时调用checkLocalTransaction检查并确认最终状态。

- **代码示例**：

- - 订单创建时，CourseOrderServiceImpl发送事务消息，本地事务方法中保存订单和订单项（courseOrderService.save(courseOrder)和courseOrderItemService.saveBatch(items)），确保订单与支付单同步；

- - 支付回调时，PayOrderFinishTransactionListener的本地事务方法更新支付单状态并记录支付流水，保证支付结果与订单状态一致。

#### 3. MQ 延迟队列处理支付超时

项目通过 RocketMQ 延迟消息替代传统定时任务，处理 30 分钟未支付订单的自动取消，流程如下：

1. **发送延迟消息**：订单创建时（CourseOrderServiceImpl），向CHECK_ORDER_STATUS_TOPIC发送延迟消息（延迟等级 3 对应 10 秒，实际可配置为 30 分钟），消息内容为订单号。

1. **消费延迟消息**：

- - 订单服务的CheckOrderStatusMessageConsumer和支付服务的CheckOrderStatusMessageConsumer监听该主题（广播模式MessageModel.BROADCASTING）；

- - 消费者接收消息后，检查订单状态，若仍为 “待支付”（OrderStatus.TO_PAY），则更新为 “超时取消”（OrderStatus.TIMEOUT_CANCEL），并调用支付宝接口关闭订单。

#### 4. 定时任务使用

项目未使用传统定时任务框架（如 Quartz），而是通过**RocketMQ 延迟消息**实现定时功能，优势在于：

- 分布式环境下无需集群协调（依赖 MQ 本身的可靠性）；

- 延迟时间可灵活配置（通过 RocketMQ 的延迟等级，如 16 级对应 30 分钟）；

- 避免定时任务集中触发导致的系统压力峰值。

#### 5. 订单防重复提交

通过**Redis Token 机制**防止重复提交，实现逻辑如下：

1. **生成 Token**：用户进入订单页时，后端生成唯一 Token（如 UUID），关联用户 ID 和课程 ID 存入 Redis（键：CacheKeys.REPEAT_SUBMIT_TOKEN+loginUserId+":"+courseIds），并返回给前端。

1. **校验 Token**：用户提交订单时，前端携带 Token，后端在checkRepeatSubmit方法中校验 Redis 中是否存在该 Token：

- - 若不存在，说明重复提交，抛出异常；

- - 若存在，校验通过后删除 Token（确保仅一次有效）。

### 二、面试必备问题解答

#### 1. 如何处理重复提交？

项目通过**Token+Redis**方案解决：

- 前端请求订单页时，后端生成唯一 Token 并缓存（关联用户 ID 和业务 ID）；

- 提交订单时，前端携带 Token，后端校验 Redis 中 Token 是否存在：

- - 存在则删除 Token 并继续处理；

- - 不存在则拒绝（重复提交）。

- 代码参考：CourseOrderServiceImpl.checkRepeatSubmit方法，通过cacheService.get和cacheService.del实现。

#### 2. 接口幂等性如何设计？

核心通过**唯一标识 + 状态校验**保证接口幂等，具体场景：

- **库存扣减**：使用 Redis+Lua 脚本，通过原子操作确保同一请求不会重复扣减库存（脚本中校验库存是否充足后再扣减）；

- **订单创建**：订单号（雪花算法生成）作为唯一标识，数据库建唯一索引，避免重复插入；

- **支付回调**：支付宝回调时，通过PayOrderService查询订单状态，若已支付则直接返回成功，避免重复处理（PayOrderFinishTransactionListener.checkLocalTransaction）。

#### 3. 支付超时如何处理？

通过**RocketMQ 延迟消息**实现超时自动取消：

- 订单创建时发送延迟 30 分钟的消息到CHECK_ORDER_STATUS_TOPIC；

- 消费者接收消息后，检查订单状态：

- - 若未支付，更新状态为 “超时取消”，释放库存；

- - 调用第三方支付接口（如支付宝）关闭订单，避免用户后续支付。

- 代码参考：CourseOrderServiceImpl发送延迟消息，CheckOrderStatusMessageConsumer处理超时逻辑。

#### 4. 秒杀流程是怎么样的？

完整流程：

1. **预热阶段**：活动发布时，将秒杀课程库存加载到 Redis（KillActivityServiceImpl）；

1. **用户秒杀**：前端倒计时→用户点击秒杀→后端通过 Redisson 信号量限流→Lua 脚本原子扣减 Redis 库存；

1. **订单生成**：扣减成功后生成临时订单→跳转订单页→正式下单并发送延迟消息（超时检查）；

1. **支付与回调**：用户支付→支付宝回调→事务消息更新订单状态→完成购买。

#### 5. 秒杀最大 QPS 及支撑手段？

- **理论 QPS**：单节点下，通过 Redis+Lua+Redisson 限流，可支撑 1 万 + QPS（具体取决于服务器配置和 Redis 性能）；

- **支撑手段**：

1. 1. **缓存前置**：库存、活动状态全量缓存到 Redis，避免 DB 直接访问；

1. 1. **限流熔断**：Redisson 信号量控制并发请求数，Sentinel 限制 QPS；

1. 1. **原子操作**：Lua 脚本保证库存扣减的原子性，防止超卖；

1. 1. **异步处理**：MQ 异步处理订单生成、支付回调，削峰填谷；

1. 1. **分布式协调**：Redisson 提供分布式锁，解决多节点并发问题。



# DAY10:面试回答

### 一、重点内容深度拆解（结合仓库项目代码 / 结构）

#### 1. 下单流程（从前端触发到支付单生成全链路）

以下是仓库项目中**秒杀场景下的下单流程**，包含核心校验、分布式事务、延迟消息等关键环节，结合具体类 / 方法 / 参数说明：

| 步骤                          | 核心操作                                                     | 关联代码 / 组件                                              | 关键校验 / 设计                                              |
| ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 1. 防重 Token 生成            | 用户进入订单确认页，前端请求后端生成唯一提交 Token           | CourseOrderController#getSubmitToken  Redis Key：cache:repeat_submit:token:{userId}:{courseIds}（过期 15 分钟） | - Token 关联用户 + 课程，防止跨用户重复提交  - Redis 设置过期时间，避免无效缓存 |
| 2. 前端提交订单               | 携带 Token、userId、courseIds、payType（1 = 支付宝 / 2 = 微信）调用下单接口 | CourseOrderController#placeOrder                             | 前端参数校验：课程 ID 非空、支付类型合法                     |
| 3. 后端参数 / 防重校验        | ① 校验 Token 是否存在（Redis 查询）② 存在则删除 Token（保证仅一次有效）③ 非空 / 合法性校验 | CourseOrderServiceImpl#checkRepeatSubmit                     | - Token 不存在则抛BusinessException("重复提交")  - 校验用户是否为登录态、课程是否属于当前秒杀活动 |
| 4. 课程信息获取               | 调用课程服务 Feign 接口，获取课程秒杀价、库存等信息          | CourseServiceAPI#getCourseInfoByIds（Feign 接口）            | - 二次校验库存（秒杀扣减后兜底）- 校验课程是否在秒杀活动有效期内 |
| 5. 订单数据组装               | ① 雪花算法生成订单号（IdUtil.getSnowflakeNextIdStr()）② 组装CourseOrder（状态 TO_PAY）、CourseOrderItem（订单项） | CourseOrderServiceImpl#buildOrderData                        | - 订单号加数据库唯一索引（防重复插入）- 总金额 = 课程秒杀价累加，精确到分 |
| 6. 事务消息发送（创建支付单） | ① 组装PayOrder（支付单，关联订单号）② 发送 RocketMQ 事务半消息 | CourseOrderServiceImpl#sendPayOrderTransactionMsg  Topic：pay_order_topic，Tags：course_order_create | - 半消息暂存 MQ，不立即投递  - 消息体携带CourseOrder+PayOrder核心数据 |
| 7. 本地事务执行               | ① 保存CourseOrder（订单表）② 保存CourseOrderItem（订单项表） | PayOrderMessageTransactionListener#executeLocalTransaction（加@Transactional） | - 本地事务失败则回滚，MQ 消息不投递  - 失败场景：数据库异常、订单号重复（唯一索引冲突） |
| 8. 支付单创建（消费事务消息） | 支付服务消费 MQ 消息，创建PayOrder（支付单）                 | CourseOrderCreateConsumer（MQ 消费者）                       | - 幂等校验：根据订单号查支付单，存在则直接返回  - 支付单状态默认 TO_PAY |
| 9. 延迟消息发送（超时兜底）   | 发送 30 分钟延迟消息，用于后续超时取消                       | CourseOrderServiceImpl#sendDelayMsg  Topic：check_order_status_topic，延迟等级 16（30 分钟） | - 延迟等级可配置（RocketMQ 16 级对应 30 分钟） - 消息体仅传订单号，减少数据传输 |
| 10. 返回结果                  | 前端跳转支付选择页，携带订单号、支付单 ID                    | CourseOrderController#placeOrder返回JSONResult.success(orderNo) | - 仅返回核心字段，避免敏感信息泄露                           |

#### 2. 支付流程（从发起支付到订单状态同步）

支付流程核心是 “第三方支付交互 + 分布式事务同步状态”，仓库项目以支付宝为例：

| 步骤                          | 核心操作                                                     | 关联代码 / 组件                                              | 关键设计                                                     |
| ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 1. 发起支付                   | 前端携带订单号调用支付接口，选择支付方式                     | PayController#applyPay                                       | - 校验订单号对应的支付单是否存在  - 校验支付单状态为 TO_PAY  |
| 2. 生成支付参数               | 支付宝：构造AlipayTradePagePayRequest，生成支付表单 HTML  微信：调用统一下单接口，生成 prepay_id | AlipayService#generatePayForm  微信：WeChatPayService#generatePayParams | - 支付单号 = 雪花算法生成，与订单号关联  - 回调地址：同步（return_url）+ 异步（notify_url） |
| 3. 用户完成支付               | 前端渲染支付表单，跳转支付宝完成支付                         | 前端支付页（pay.html）                                       | - 同步回调仅做页面跳转，不处理业务  - 核心状态更新依赖异步回调 |
| 4. 支付宝异步回调             | ① 验证签名（防伪造请求）② 解析支付结果（交易号、支付状态）   | AliPayCallBackController#callback  签名校验：AlipaySignature.rsaCheckV1 | - 仅处理TRADE_SUCCESS状态（支付成功） - 非 200 响应支付宝会重试（最多 8 次） |
| 5. 支付结果事务消息           | 发送事务半消息，同步支付结果到订单服务                       | PayCallbackServiceImpl#sendPayFinishTransactionMsg  Topic：pay_order_topic，Tags：course_order_pay_finish | - 消息体携带支付单号、交易号、支付状态                       |
| 6. 支付单状态更新（本地事务） | ① 乐观锁更新PayOrder状态为 PAY_SUCCESS ② 保存PayFlow（支付流水） | PayOrderFinishTransactionListener#executeLocalTransaction    | - 乐观锁条件：pay_order_no = ? and status = TO_PAY  - 流水记录用于对账，不可修改 |
| 7. 订单状态同步               | 订单服务消费 MQ 消息，更新CourseOrder状态为 PAY_SUCCESS      | CourseOrderPayFinishConsumer（MQ 消费者）                    | - 幂等校验：订单状态非 TO_PAY 则直接返回  - 同步更新课程购买关系（user_course_rel表），开通课程权限 |

#### 3. 延迟消息（支付超时核心）

仓库项目基于 RocketMQ 延迟消息实现支付超时处理，核心设计：

- **发送时机**：下单流程第 9 步，订单创建成功后立即发送；

- **消息配置**：

- - Topic：check_order_status_topic（广播模式，保证订单 / 支付服务都能消费）；

- - 延迟等级：16（对应 30 分钟，RocketMQ 延迟等级规则：1=1s、2=5s、…、16=30m）；

- - 消息体：仅订单号（轻量化，减少 MQ 存储压力）；

- **消费逻辑**（双服务协同）：

| 消费服务 | 核心操作                                                     | 关联代码                             |
| -------- | ------------------------------------------------------------ | ------------------------------------ |
| 支付服务 | ① 查询PayOrder状态为 TO_PAY ② 调用支付宝关闭订单接口 ③ 更新PayOrder为 TIMEOUT_CANCEL | CheckOrderStatusConsumer（支付服务） |
| 订单服务 | ① 查询CourseOrder状态为 TO_PAY ② 更新为 TIMEOUT_CANCEL ③ 回滚秒杀库存（Redis+DB） | CheckOrderStatusConsumer（订单服务） |

- **兜底机制**：延迟消息消费失败则进入死信队列，定时任务（XXL-Job）每 5 分钟扫描死信队列，重试处理。

#### 4. 事务消息（分布式事务核心）

仓库项目采用**RocketMQ 事务消息**解决跨服务（订单服务↔支付服务）分布式事务，核心是 “半消息 + 本地事务 + 事务回查”，分两个核心场景：

| 场景                  | 半消息发送方 | 本地事务逻辑                    | 事务回查逻辑                     | 消费方操作                  |
| --------------------- | ------------ | ------------------------------- | -------------------------------- | --------------------------- |
| 订单创建→支付单创建   | 订单服务     | 保存CourseOrder+CourseOrderItem | 查询订单是否存在，存在则提交消息 | 支付服务创建PayOrder        |
| 支付成功→订单状态更新 | 支付服务     | 更新PayOrder状态 + 保存PayFlow  | 查询支付单状态，成功则提交消息   | 订单服务更新CourseOrder状态 |

- **核心机制**：

1. 1. 半消息：发送后 MQ 暂存，不立即投递，等待本地事务结果；

1. 1. 本地事务：加@Transactional保证单库操作原子性，成功则返回LocalTransactionState.COMMIT_MESSAGE，失败则返回ROLLBACK_MESSAGE；

1. 1. 事务回查：若本地事务结果未知（如服务宕机），MQ 每 60s 调用checkLocalTransaction方法，最多重试 15 次，确认后决定提交 / 回滚；

1. 1. 幂等消费：所有 MQ 消费者都通过 “唯一业务号（订单号 / 支付单号）” 做查询校验，避免重复处理。

### 二、面试必备问题（详细解答）

#### 1. 讲一下下单和支付的流程（尽可能详细）

##### （1）下单流程（秒杀场景）

1. **前置防重准备**：用户从秒杀成功页跳转到订单确认页，前端请求后端生成唯一的防重复提交 Token，后端生成 UUID Token，关联用户 ID 和课程 ID 存入 Redis（过期 15 分钟），并返回 Token 给前端；

1. **前端提交订单**：用户确认订单信息（课程、金额、支付方式），前端携带 Token、用户 ID、课程 ID、支付类型（支付宝 / 微信）调用下单接口；

1. **后端基础校验**：

- - 校验 Token 是否存在（Redis 查询），不存在则直接返回 “重复提交”；存在则删除 Token（保证仅一次有效）；

- - 校验参数合法性：用户是否登录、课程是否属于当前秒杀活动、库存是否充足（二次兜底校验）；

1. **课程信息获取**：通过 Feign 调用课程服务，获取课程秒杀价、名称等信息，计算订单总金额；

1. **订单数据组装**：用雪花算法生成全局唯一订单号（数据库加唯一索引），组装订单主表（CourseOrder，状态为 “待支付”）和订单项表（CourseOrderItem）数据；

1. **分布式事务（创建支付单）**：

- - 组装支付单（PayOrder），关联订单号，发送 RocketMQ 事务半消息到支付服务；

- - 触发本地事务：保存订单主表和订单项表，本地事务成功则 MQ 提交消息，失败则回滚；

1. **支付单创建**：支付服务消费 MQ 消息，做幂等校验（根据订单号查支付单，避免重复创建），创建支付单（状态 “待支付”）；

1. **超时兜底**：发送 30 分钟延迟消息到 MQ，用于后续支付超时自动取消；

1. **结果返回**：后端返回订单号、支付单 ID 给前端，前端跳转至支付选择页。

##### （2）支付流程（以支付宝为例）

1. **发起支付**：前端携带订单号调用支付接口，后端校验支付单是否存在、状态是否为 “待支付”，确认后生成支付宝支付参数；

1. **生成支付表单**：后端构造支付宝支付请求（包含订单号、金额、回调地址），生成支付表单 HTML 返回给前端；

1. **用户完成支付**：前端渲染表单，用户跳转至支付宝页面完成支付，支付完成后支付宝跳转前端同步回调页（仅做页面展示）；

1. **支付宝异步回调**：

- - 支付宝调用后端异步回调接口，后端首先验证签名（防止伪造请求），仅处理 “支付成功” 状态；

- - 解析回调参数（支付单号、支付宝交易号、支付金额），校验金额是否与订单一致；

1. **支付结果同步（分布式事务）**：

- - 发送 RocketMQ 事务半消息，触发本地事务：用乐观锁更新支付单状态为 “已支付”，并保存支付流水（用于对账）；

- - 本地事务成功则 MQ 提交消息，失败则回滚；

1. **订单状态同步**：订单服务消费 MQ 消息，做幂等校验（订单状态非 “待支付” 则直接返回），更新订单状态为 “已支付”，并调用课程服务为用户开通课程权限；

1. **前端通知**：通过 WebSocket 通知前端支付成功，跳转至订单成功页。

#### 2. 支付超时是如何处理的？

仓库项目通过 “RocketMQ 延迟消息 + 双服务协同 + 定时任务兜底” 处理支付超时，核心步骤：

1. **超时触发点**：下单成功后，立即发送 30 分钟延迟消息到 MQ（广播模式），消息体仅包含订单号；

1. **延迟消息消费（核心）**：

- - 支付服务消费：查询支付单状态，若仍为 “待支付”，调用支付宝关闭订单接口（防止用户后续支付），并更新支付单状态为 “超时取消”；

- - 订单服务消费：查询订单状态，若仍为 “待支付”，更新订单状态为 “超时取消”，并回滚秒杀库存（Redis + 数据库原子加 1）；

1. **幂等保障**：消费时先校验状态，非 “待支付” 则直接返回，避免重复处理；

1. **兜底机制**：若延迟消息消费失败（如服务宕机），补充 XXL-Job 分布式定时任务，每 5 分钟扫描 “待支付” 且创建时间超过 30 分钟的支付单 / 订单，执行关闭订单、更新状态、回滚库存操作；

1. **用户感知**：超时取消后，通过短信 / 站内信通知用户订单已取消，库存已释放。

#### 3. 你们项目中是如何处理分布式事务的？

仓库项目核心采用**RocketMQ 事务消息**解决跨服务（订单服务↔支付服务）的分布式事务问题，替代传统 2PC（性能差）和 TCC（开发复杂），核心思路是 “半消息 + 本地事务 + 事务回查”，具体实现：

1. **核心场景**：

- - 场景 1：订单创建与支付单创建的一致性（下单环节）：保证 “订单创建成功则支付单必创建，订单创建失败则支付单不创建”；

- - 场景 2：支付结果与订单状态的一致性（支付回调环节）：保证 “支付单更新为成功则订单必更新为成功，支付单更新失败则订单不更新”；

1. **实现流程**：

- - 发送半消息：业务服务（订单 / 支付）发送事务半消息到 MQ，MQ 仅暂存消息，不立即投递到消费方；

- - 执行本地事务：半消息发送成功后，触发本地事务（如保存订单、更新支付单），本地事务加@Transactional保证单库原子性；

- - 提交 / 回滚消息：本地事务成功则通知 MQ 提交消息（消费方可消费），失败则通知 MQ 回滚消息（消费方收不到）；

- - 事务回查：若本地事务结果未知（如服务宕机），MQ 定时调用回查方法，查询数据库中业务数据状态，确认后决定提交 / 回滚；

1. **配套保障**：

- - 幂等性：所有 MQ 消费方通过 “唯一业务号（订单号 / 支付单号）” 做查询校验，避免重复处理；更新操作使用乐观锁（状态条件 /version 字段）；

- - 异常补偿：消费失败的消息进入死信队列，定时任务重试处理，重试失败则人工介入；

- - 最终一致性：允许短时间内数据不一致（如订单已创建但支付单未创建），但通过事务消息和回查机制，最终保证数据一致。

总结：该方案适合秒杀高并发场景，通过异步消息削峰，依赖 MQ 的可靠性保证事务最终一致性，相比传统方案性能更高、开发成本更低。



​	
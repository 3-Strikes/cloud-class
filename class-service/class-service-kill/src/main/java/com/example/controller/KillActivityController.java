package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.KillActivity;
import com.example.enums.PublishStatus;
import com.example.query.KillActivityQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.KillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/killActivity")
public class KillActivityController {

    @Autowired
    public KillActivityService killActivityService;

    /**
     * 第一步：后台页面发布秒杀活动
     *
     * redis三个数据：
     * 秒杀课程集合：hash类型  key：ymcc:kill:activity:活动id              field：课程id       value：killCourse对象
     * 秒杀课程的库存量：string类型   key:ymcc:kill:coursecount:活动id:课程id   value：秒杀库存量
     * 秒杀课程的详情信息（章节，价格，资料，详情信息）：hash类型  key：ymcc:kill:detail:活动id   field：课程id，          value：CourseDetailVO（）
     *
     * 第二步：前端课程站点查看秒杀课程信息
     * 1.查询redis中的秒杀课程集合
     *
     * 第三步：前端秒杀课程详情页面查看秒杀课程详情信息
     * 1.从redis的秒杀课程集合中获取课程killCourse对象（秒杀价格信息，秒杀时间信息）
     * 2.查询redis中的秒杀课程的详情信息CourseDetailVO对象（CourseDetail，Course，CourseMarket）
     *
     * 第四步：用户点击按钮，进行秒杀课程操作
     * 1.sentinel限流，通过BlockExceptionHandler处理流控异常
     * 2.redis信号量限流，许可获取失败，直接返回秒杀失败
     * 3.判断库存，库存不足，返回秒杀失败
     * 4.秒杀成功，
     *      4.1 生成临时订单(订单号--活动id，课程id，购买人id)，订单号返回前端
     *      4.2 发送mq延时任务，1分钟检测订单确认状态，避免少卖问题。TODO
     * 5.前端获取订单号，跳转到订单确认页面
     *      5.1 生成防重复提交token
     *      5.2 带着订单号，带着token，带着courseId，带着付款类型，提交秒杀订单
     *      5.3 要显示秒杀课程信息，价格显示为秒杀价格 TODO
     * 6.提交秒杀订单接口：
     *      6.1 根据orderNo临时订单号查询订单信息（活动id，课程id，购买人id）
     *      6.2 根据课程id，活动id，获得KillCourse对象（要秒杀价格，课程名，课程图片）
     *      6.3 创建主订单对象，创建子订单对象，保存订单信息
     *      6.4 发送mq事务消息，生成支付单
     *      6.5 发送mq演示消息，检测支付状态。TODO
     *
     *
     *
     * @param actId
     * @return
     */
    @PostMapping("publish/{actId}")
    public JSONResult publish(@PathVariable("actId") Long actId){
        killActivityService.publish(actId);
        return JSONResult.success();
    }

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody KillActivity killActivity){
        if(killActivity.getId()!=null){
            killActivityService.updateById(killActivity);
        }else{
            killActivity.setPublishStatus(PublishStatus.TO_PUBLISH.getCode());
            killActivityService.save(killActivity);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        killActivityService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(killActivityService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(killActivityService.list());
    }


    /**
    * 带条件分页查询数据
     * @DateTimeFormat:  非json格式的参数处理。content-type=applicaiton/www-form-urlencoded
     * @JSONFormat:  json格式的参数处理。content-type=application/json
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody KillActivityQuery query){
        Page<KillActivity> page = new Page<KillActivity>(query.getPage(),query.getRows());
        page = killActivityService.page(page);
        return JSONResult.success(new PageList<KillActivity>(page.getTotal(),page.getRecords()));
    }
}

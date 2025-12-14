package com.example.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.CourseOrder;
import com.example.dto.CourseOrderDTO;
import com.example.query.CourseOrderQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.CourseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseOrder")
public class CourseOrderController {

    @Autowired
    public CourseOrderService courseOrderService;


    @PostMapping("placeOrder")
    public JSONResult placeOrder(@Valid @RequestBody CourseOrderDTO courseOrderDTO){
        //防重复提交的token校验
        String loginUserId="100";
        courseOrderService.checkRepeatSubmit(courseOrderDTO.getToken(), loginUserId,StrUtil.join(",",courseOrderDTO.getCourseIds()));

        //生成课程订单
        String orderNo=courseOrderService.placeOrder(loginUserId,courseOrderDTO);
        return JSONResult.success(orderNo);
    }

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseOrder courseOrder){
        if(courseOrder.getId()!=null){
            courseOrderService.updateById(courseOrder);
        }else{
            courseOrderService.save(courseOrder);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseOrderService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseOrderService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseOrderService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseOrderQuery query){
        Page<CourseOrder> page = new Page<CourseOrder>(query.getPage(),query.getRows());
        page = courseOrderService.page(page);
        return JSONResult.success(new PageList<CourseOrder>(page.getTotal(),page.getRecords()));
    }
}

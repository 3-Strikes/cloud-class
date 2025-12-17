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

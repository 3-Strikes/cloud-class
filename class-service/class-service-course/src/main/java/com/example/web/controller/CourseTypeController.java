package com.example.web.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.example.service.CourseTypeService;
import com.example.domain.CourseType;
import com.example.query.CourseTypeQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/courseType")
public class CourseTypeController {

    @Autowired
    public CourseTypeService courseTypeService;

    @GetMapping("treeData")
    public JSONResult treeData(){
        List<CourseType> tree=courseTypeService.buildTreeData();
        return JSONResult.success(tree);
    }
    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseType courseType){
        if(courseType.getId()!=null){
            courseTypeService.updateById(courseType);
        }else{
            courseTypeService.save(courseType);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseTypeService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseTypeService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseTypeService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseTypeQuery query){
        Page<CourseType> page = new Page<CourseType>(query.getPage(),query.getRows());
        page = courseTypeService.page(page);
        return JSONResult.success(new PageList<CourseType>(page.getTotal(),page.getRecords()));
    }

    /**
     * 批量删除分类
     */
    @RequestMapping(value="/batch/{ids}",method=RequestMethod.DELETE)
    public JSONResult batchDelete(@PathVariable("ids") String ids){
        if(!StringUtils.hasText(ids)){
            return JSONResult.error("删除ID不能为空");
        }
        try {
            // 拆分ID字符串为Long列表
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            // 批量删除
            boolean success = courseTypeService.removeByIds(idList);
            if(success){
                return JSONResult.success("批量删除成功");
            }else{
                return JSONResult.error("批量删除失败");
            }
        } catch (Exception e) {
            return JSONResult.error("批量删除异常：" + e.getMessage());
        }
    }

    /**
     * 根据PID查询子分类（补充实现）
     */
    @GetMapping("selectChildrenById/{pid}")
    public JSONResult selectChildrenById(@PathVariable("pid") Long pid){
        List<CourseType> list = courseTypeService.lambdaQuery()
                .eq(CourseType::getPid, pid)
                .list();
        return JSONResult.success(list);
    }
}

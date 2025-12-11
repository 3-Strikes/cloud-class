package com.example.web.controller;

import com.example.domain.CourseChapter;
import com.example.mapper.CourseChapterMapper;
import com.example.query.CourseChapterQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.CourseChapterService;
import com.example.service.CourseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courseChapter")
public class CourseChapterController {

    @Autowired
    private CourseChapterService courseChapterService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseChapterMapper courseChapterMapper;

    @GetMapping("listByCourseId/{courseId}")
    public JSONResult listChapterByCourseId(@PathVariable("courseId") Long courseId){
        List<CourseChapter> list=courseChapterService.listByCourseId(courseId);
        return JSONResult.success(list);
    }
    /**
     * 保存和修改章节（自动更新课程章节数）
     */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseChapter courseChapter){
        Long oldCourseId = null;
        // 编辑场景：记录原课程ID（用于更新原课程章节数）
        if(courseChapter.getId()!=null){
            CourseChapter oldChapter = courseChapterService.getById(courseChapter.getId());
            oldCourseId = oldChapter.getCourseId();
        }

        // 新增场景：自动生成章节号（最大章节号+1）
        if(courseChapter.getId()==null){
            Integer maxNumber = courseChapterService.getMaxChapterNumber(courseChapter.getCourseId());
            courseChapter.setNumber(maxNumber + 1);
        }

        // 保存/更新章节
        boolean success = false;
        if(courseChapter.getId()!=null){
            success = courseChapterService.updateById(courseChapter);
        }else{
            success = courseChapterService.save(courseChapter);
        }

        if(success){
            // 更新新课程的章节数
            courseService.updateCourseChapterCount(courseChapter.getCourseId());
            // 编辑且课程ID变更时，更新原课程的章节数
            if(oldCourseId!=null && !oldCourseId.equals(courseChapter.getCourseId())){
                courseService.updateCourseChapterCount(oldCourseId);
            }
            return JSONResult.success();
        }else{
            return JSONResult.error("保存失败");
        }
    }

    /**
     * 删除单个章节
     */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        // 先查询章节信息（获取课程ID）
        CourseChapter chapter = courseChapterService.getById(id);
        if(chapter == null){
            return JSONResult.error("章节不存在");
        }

        // 删除章节
        boolean success = courseChapterService.removeById(id);
        if(success){
            // 更新课程章节数
            courseService.updateCourseChapterCount(chapter.getCourseId());
            return JSONResult.success();
        }else{
            return JSONResult.error("删除失败");
        }
    }

    /**
     * 批量删除章节
     */
    @RequestMapping(value="/batchDelete",method=RequestMethod.POST)
    public JSONResult batchDelete(@RequestBody List<Long> ids){
        if(ids == null || ids.isEmpty()){
            return JSONResult.error("请选择要删除的章节");
        }

        // 查询所有要删除的章节（获取关联的课程ID）
        List<CourseChapter> chapters = courseChapterService.listByIds(ids);

        // 批量删除
        boolean success = courseChapterService.removeByIds(ids);
        if(success){
            // 批量更新涉及的课程章节数
            chapters.stream()
                    .map(CourseChapter::getCourseId)
                    .distinct()
                    .forEach(courseService::updateCourseChapterCount);
            return JSONResult.success();
        }else{
            return JSONResult.error("批量删除失败");
        }
    }

    /**
     * 获取单个章节
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id") Long id){
        return JSONResult.success(courseChapterService.getById(id));
    }

    /**
     * 查询所有章节
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseChapterService.list());
    }

    /**
     * 带条件分页查询
     */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody(required = false) CourseChapterQuery query){
        if (query == null) {
            query = new CourseChapterQuery();
        }

        if (query.getPage() == null || query.getPage() <= 0) {
            query.setPage(1);
        }

        if (query.getRows() == null || query.getRows() <= 0) {
            query.setRows(10);
        }

        Page<CourseChapter> page = new Page<>(query.getPage(), query.getRows());
        page = courseChapterService.page(page);
        return JSONResult.success(new PageList<>(page.getTotal(), page.getRecords()));
    }

    /**
     * 查询课程下最大章节号
     */
    @RequestMapping(value = "/maxNumber/{courseId}",method = RequestMethod.GET)
    public JSONResult getMaxChapterNumber(@PathVariable("courseId") Long courseId){
        Integer maxNumber = courseChapterService.getMaxChapterNumber(courseId);
        return JSONResult.success(maxNumber);
    }
}
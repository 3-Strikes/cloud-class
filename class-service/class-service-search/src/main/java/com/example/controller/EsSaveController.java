package com.example.controller;

import com.example.config.CourseSearchRepository;
import com.example.doc.CourseDoc;
import com.example.result.JSONResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("es")
public class EsSaveController {
    @Autowired
    private CourseSearchRepository resos;

    @PostMapping("publishCourse2Es")
    public JSONResult publishCourse2Es(@RequestBody List<CourseDoc> CourseDocList){
//        (课程名，课程分类，讲师，免费收费，现价，原价,时间，forUser，等级，销量数，评论数，浏览量，上架时间)
        // 把CourseDocList保存为es文档。

        resos.saveAll(CourseDocList);
        return JSONResult.success();
    }
}

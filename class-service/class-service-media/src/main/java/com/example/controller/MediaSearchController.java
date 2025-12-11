package com.example.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.MediaFile;
import com.example.enums.FileStatus;
import com.example.result.JSONResult;
import com.example.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mediaSearch")
public class MediaSearchController {
    @Autowired
    private MediaFileService mediaFileService;

    @PostMapping("/listPushEnd")
    public JSONResult listPushEnd(@RequestBody List<Long> coursIds){
        //根据courseIds，查询出所有已推流的课程,查询filestatus!=2
        List<MediaFile> list = mediaFileService.list(Wrappers.lambdaQuery(MediaFile.class).in(MediaFile::getCourseId, coursIds).ne(MediaFile::getFileStatus, FileStatus.PUSH_END.getCode()));
        //提取课程id的set结合
        Set<Long> resultCourseIds = list.stream().map(MediaFile::getCourseId).collect(Collectors.toSet());
        coursIds.removeAll(resultCourseIds);
        return JSONResult.success(coursIds);
    }
}

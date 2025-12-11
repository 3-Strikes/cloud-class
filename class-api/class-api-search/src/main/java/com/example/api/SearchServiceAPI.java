package com.example.api;

import com.example.doc.CourseDoc;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "service-search")
public interface SearchServiceAPI {
    @PostMapping("/es/publishCourse2Es")
    JSONResult publishCourse2Es(@RequestBody List<CourseDoc> CourseDocList);
}

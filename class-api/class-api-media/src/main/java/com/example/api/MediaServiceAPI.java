package com.example.api;

import com.example.domain.MediaFile;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(name = "service-media")
public interface MediaServiceAPI {
    @PostMapping("/mediaSearch/listPushEnd")
    JSONResult<Set<Long>> listPushEnd(@RequestBody List<Long> coursIds);

    @GetMapping("/mediaSearch/mediafiles/{courseId}")
    JSONResult<List<MediaFile>> listMediaFiles(@PathVariable("courseId") Long courseId);
}

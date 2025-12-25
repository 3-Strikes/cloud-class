// 新增文件：class-api/class-api-kill/src/main/java/com/example/KillActivityAPI.java
package com.example;

import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "service-kill")
public interface KillActivityAPI {

}
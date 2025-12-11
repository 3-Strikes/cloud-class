package com.example.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.result.JSONResult;
import com.example.util.OssUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("oss")
public class OssController {

    @PostMapping("uploadFile")
    public JSONResult uploadFile(MultipartFile fileName) throws IOException {
        String format = DateUtil.format(new Date(), "yyyy/MM/dd");
        String originalFilename = fileName.getOriginalFilename();
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuidFileName= IdUtil.fastSimpleUUID()+fileExt;
        String objectName= format+"/"+uuidFileName;

        String url = OssUtil.upload(objectName, fileName.getBytes());
        return JSONResult.success(url);//返回什么信息？http地址
    }

    // 新增：OSS文件删除接口
    @DeleteMapping("deleteFile")
    public JSONResult deleteFile(@RequestParam String fileUrl) {
        try {
            // 1. 从URL中解析出OSS的objectName（调用OssUtil的工具方法）
            String objectName = OssUtil.subObjectName(fileUrl);
            if (objectName == null || objectName.isEmpty()) {
                return JSONResult.error("文件URL格式错误，无法解析");
            }

            // 2. 调用OssUtil删除文件
            OssUtil.del(objectName);
            return JSONResult.success("文件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            // 非强制失败（文件删除失败不影响课程删除）
            return JSONResult.error("文件删除失败：" + e.getMessage());
        }
    }


}

package com.example.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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

    /**
     * 文件删除接口（RESTful风格推荐使用DELETE请求）
     * @param objectName OSS中存储的文件对象名（如：2025/12/11/xxxx.jpg）
     * @return JSONResult 删除结果
     */
    @DeleteMapping("deleteFile")
    public JSONResult deleteFile(@RequestParam("objectName") String objectName) {
        // 校验对象名是否为空
        if (StrUtil.isBlank(objectName)) {
            return JSONResult.error("文件对象名不能为空");
        }

        try {
            // 调用OSS工具类删除文件
            OssUtil.del(objectName);
            return JSONResult.success("文件删除成功");
        } catch (Exception e) {
            // 捕获OSS操作异常，返回友好提示
            return JSONResult.error("文件删除异常：" + e.getMessage());
        }
    }

    @DeleteMapping("delete")
    public JSONResult delete(String url){
        String objectName = OssUtil.subObjectName(url);
        OssUtil.del(objectName);
        return JSONResult.success("删除成功");
    }
}

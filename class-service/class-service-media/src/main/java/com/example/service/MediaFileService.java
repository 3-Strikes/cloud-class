package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.MediaFile;
import com.example.result.JSONResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-09
 */
public interface MediaFileService extends IService<MediaFile> {
    /**
     * 文件上传之前的注册功能
     */
    JSONResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    /**
     * 校验文件块是否已经存在了
     */
    JSONResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);

    /**
     * 上传文件块
     */
    JSONResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk);

    /**
     * 合并文件快
     */
    JSONResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt,
                           Long courseChapterId, Long courseId, String name, String courseName, String chapterName);

    //处理文件
    JSONResult handleFile2m3u8(MediaFile mediaFile);
}

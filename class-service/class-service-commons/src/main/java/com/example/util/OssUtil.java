package com.example.util;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.example.config.OSSProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class OssUtil {
    private static final Logger logger = LoggerFactory.getLogger(OssUtil.class);
    private static OSS ossClient = null;
    private static String bucketName = null;
    private static final String urlPrefix = "https://class-cloud.oss-cn-shanghai.aliyuncs.com/";

    @Autowired
    private OSSProperties ali;

    public static String subObjectName(String url) {
        return url.replace(urlPrefix, "");
    }

    //启动容器时执行初始化方法
    @PostConstruct
    public void init() {
        // 不在初始化阶段创建OSS客户端，而是在首次使用时创建
        logger.info("OSS Util initialized, will create client on first use");
    }
    
    private synchronized void initializeOSSClientIfNeeded() {
        if (ossClient != null) {
            return;
        }
        
        try {
            // 等待配置注入完成
            int attempts = 0;
            while ((ali.getAkid() == null || ali.getAkid().isEmpty()) && attempts < 10) {
                Thread.sleep(500);
                attempts++;
            }
            
            // 再次检查是否已经初始化（防止多线程重复初始化）
            if (ossClient != null) {
                return;
            }
            
            if (ali.getAkid() == null || ali.getAkid().isEmpty()) {
                throw new IllegalStateException("OSS access key ID is not configured properly");
            }
            
            if (ali.getAkSecret() == null || ali.getAkSecret().isEmpty()) {
                throw new IllegalStateException("OSS access key secret is not configured properly");
            }
            
            if (ali.getEndpoint() == null || ali.getEndpoint().isEmpty()) {
                throw new IllegalStateException("OSS endpoint is not configured properly");
            }
            
            bucketName = ali.getBucketName();
            
            logger.info("Initializing OSS client with endpoint: {}, bucket: {}", ali.getEndpoint(), bucketName);

            DefaultCredentialProvider provider = CredentialsProviderFactory.newDefaultCredentialProvider(ali.getAkid(), ali.getAkSecret());

            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
            ossClient = OSSClientBuilder.create()
                    .endpoint(ali.getEndpoint())
                    .credentialsProvider(provider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(ali.getRegion())
                    .build();
                    
            logger.info("OSS client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize OSS client", e);
            throw new RuntimeException("Failed to initialize OSS client", e);
        }
    }

    public static String upload(String objectName, byte[] content) {
        ensureOSSClientInitialized();
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(content));
        ossClient.putObject(putObjectRequest);
        return urlPrefix + objectName;
    }

    public static String upload(String objectName, InputStream stream) {
        ensureOSSClientInitialized();
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName,stream);
        ossClient.putObject(putObjectRequest);
        return urlPrefix + objectName;
    }

    public static void del(String objectName) {
        ensureOSSClientInitialized();
        ossClient.deleteObject(bucketName, objectName);
    }
    
    private static void ensureOSSClientInitialized() {
        // 获取当前实例以访问非静态方法
        OssUtil instance = SpringContextUtil.getBean(OssUtil.class);
        if (instance != null) {
            instance.initializeOSSClientIfNeeded();
        }
        
        if (ossClient == null) {
            throw new IllegalStateException("OSS client is not initialized. Check your OSS configuration.");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            ossClient = null;
        }
    }
}
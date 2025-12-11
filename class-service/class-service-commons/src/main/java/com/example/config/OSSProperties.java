package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oss")
@RefreshScope
public class OSSProperties {
    private String akid;
    private String akSecret;
    private String endpoint;
    private String region;
    private String bucketName;

    public String getAkid() {
        return akid;
    }

    public void setAkid(String akid) {
        this.akid = akid;
    }

    public String getAkSecret() {
        return akSecret;
    }

    public void setAkSecret(String akSecret) {
        this.akSecret = akSecret;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}

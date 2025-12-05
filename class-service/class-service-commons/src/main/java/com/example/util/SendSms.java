package com.example.util;

import cn.hutool.extra.spring.SpringUtil;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.example.config.SmsProperties;
import com.example.exceptions.BusinessException;
import darabonba.core.client.ClientOverrideConfiguration;

import java.util.concurrent.CompletableFuture;

public class SendSms {
    private static SmsProperties bean = null;
    private static  AsyncClient client =null;

    public static void init(){
        if(client!=null)
            return;
        if(bean==null)
            bean = SpringUtil.getBean(SmsProperties.class);

        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(bean.getAccessKeyId())
                .accessKeySecret(bean.getAccessKeySecret())
                .build());

        client = AsyncClient.builder()
                .region("ap-southeast-1") // Region ID
                .credentialsProvider(provider)
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride("dypnsapi.aliyuncs.com")
                )
                .build();
    }

    public static void sendValiCode(String phone, String code) {
        init();

        SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = SendSmsVerifyCodeRequest.builder()
                .signName(bean.getSignName())
                .templateCode(bean.getTemplateCode())
                .phoneNumber(phone)
                .templateParam("{\"code\":"+code+",\"min\":\"5\"}")
                .build();

        CompletableFuture<SendSmsVerifyCodeResponse> response = client.sendSmsVerifyCode(sendSmsVerifyCodeRequest);
        SendSmsVerifyCodeResponse resp = null;
        try {
            resp = response.get();
        } catch (Exception e) {
           throw new BusinessException("短信发送失败");
        }
    }
}

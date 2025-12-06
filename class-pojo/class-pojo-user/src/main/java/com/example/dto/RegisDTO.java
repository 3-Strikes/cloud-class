package com.example.dto;

import com.example.validation.Password;
import com.example.validation.Phone;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisDTO {
    @NotBlank(message = "手机号不能为空")
    @Phone(message = "手机号格式不正确")
    private String mobile;

    @NotBlank(message = "密码不能为空")
    @Password(min = 6, max = 20, message = "密码长度必须在6-20位之间，且包含数字和字母")
    private String password;

    @NotBlank(message = "图片验证码不能为空")
    private String imageCode;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    private Integer regChannel;
}
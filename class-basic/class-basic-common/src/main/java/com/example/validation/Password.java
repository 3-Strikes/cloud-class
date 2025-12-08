package com.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    // 密码最小长度
    int min() default 6;
    // 密码最大长度
    int max() default 20;
    // 是否需要包含数字
    boolean needNumber() default true;
    // 是否需要包含字母
    boolean needLetter() default true;
    
    String message() default "密码格式不正确";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
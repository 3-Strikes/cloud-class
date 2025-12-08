package com.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private int min;
    private int max;
    private boolean needNumber;
    private boolean needLetter;
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    
    @Override
    public void initialize(Password constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.needNumber = constraintAnnotation.needNumber();
        this.needLetter = constraintAnnotation.needLetter();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        
        // 长度校验
        if (password.length() < min || password.length() > max) {
            return false;
        }
        
        // 数字校验
        if (needNumber && !NUMBER_PATTERN.matcher(password).find()) {
            return false;
        }
        
        // 字母校验
        if (needLetter && !LETTER_PATTERN.matcher(password).find()) {
            return false;
        }
        
        return true;
    }
}
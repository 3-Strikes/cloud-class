package com.example.validation;

import com.example.util.AssertUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        try {
            AssertUtil.isPhone(phone, "");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
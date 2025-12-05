package com.example.exceptions;

import com.example.enums.E;
import com.example.result.JSONResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
//@ResponseBody //把返回结果封装成json
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public JSONResult handleException(BusinessException e){
        e.printStackTrace();
        //进日志文件
        return JSONResult.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JSONResult handleException(MethodArgumentNotValidException e){
        Map<String,String> info=new HashMap<>();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            info.put(field,defaultMessage);
        }
        return JSONResult.error(E.ARG_VALI_ERROR,info);
    }
    @ExceptionHandler(RuntimeException.class)
    public JSONResult handleException(RuntimeException e){
        e.printStackTrace();
        E error = E.ERROR;
        error.setMsg(e.getMessage());
        return JSONResult.error(E.ERROR);
    }

    //sentinel限流异常，熔断异常
    @ExceptionHandler(Exception.class)
    public JSONResult handleException(Exception e){
        e.printStackTrace();
        return JSONResult.error(E.ERROR);
    }
}

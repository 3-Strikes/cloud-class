package com.example.result;

import com.example.enums.E;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

//返回JSON结果 Ret
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSONResult<T> {

    private boolean success = true;

    private String message = "成功";

    //错误码，用来描述错误类型 ，20000 表示么有错误
    private String code = "20000";

    //返回的数据
    private T data;

    /**
     * 创建当前实例
     **/
    public static JSONResult success() {
        return new JSONResult();
    }

    /**
     * 创建当前实例
     **/
    public static JSONResult success(Object obj) {
        JSONResult instance = new JSONResult();
        instance.setData(obj);
        return instance;
    }

    public static JSONResult success(Object obj, String code) {
        JSONResult instance = new JSONResult();
        instance.setCode(code);
        instance.setData(obj);
        return instance;
    }

    /**
     * 创建当前实例
     **/

    public static JSONResult error(String message, String code) {
        JSONResult instance = new JSONResult();
        instance.setMessage(message);
        instance.setSuccess(false);
        instance.setCode(code);
        return instance;
    }

    public static JSONResult error(String message,String code,Object data) {
        JSONResult instance = new JSONResult();
        instance.setMessage(message);
        instance.setSuccess(false);
        instance.setCode(code);
        instance.setData(data);
        return instance;
    }


    public static JSONResult error() {
        JSONResult jsonResult = new JSONResult();
        jsonResult.setSuccess(false);
        return jsonResult;
    }

    /**
     * 创建当前实例
     **/
    public static JSONResult error(String message) {
        return error(message, null);
    }

//    public static JSONResult error(CommonErrorCode errorCode){
//        return error(errorCode.getMesssage(),errorCode.getCode());
//    }

    public static JSONResult error(E e) {return error(e.getMsg(),e.getCode());}

    public static JSONResult error(E e,Object data) {return error(e.getMsg(),e.getCode(),data);}

}

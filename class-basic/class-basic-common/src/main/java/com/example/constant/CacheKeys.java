package com.example.constant;

public interface CacheKeys {
    //项目名：业务：唯一标识
    String APP_ID="ymcc:";
    String SERVICE_ID="commons:";

    String COURSE_SERVICE_ID="course:";
    String VALI_CODE=APP_ID+SERVICE_ID+"valicode:";

    String COURSE_TYPE_TREE = APP_ID+COURSE_SERVICE_ID+"courseTypeTree";
    String REPEAT_SUBMIT_TOKEN = APP_ID + SERVICE_ID + "repeat_submit:";
}

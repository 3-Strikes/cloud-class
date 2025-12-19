package com.example.constant;

public interface CacheKeys {
    //项目名：业务：唯一标识
    String APP_ID="ymcc:";
    String SERVICE_ID="commons:";
    String COURSE_SERVICE_ID="course:";
    String VALI_CODE=APP_ID+SERVICE_ID+"valicode:";
    String KILL_SERVICE_ID="kill:";

    String COURSE_TYPE_TREE = APP_ID+COURSE_SERVICE_ID+"courseTypeTree";
    String REPEAT_SUBMIT_TOKEN = APP_ID + SERVICE_ID + "repeat_submit:";

    String KILL_ACTIVITY = APP_ID+KILL_SERVICE_ID+"activity:";

    //
    String KILL_ACTIVITY_COURSE_DETAIL =  APP_ID+KILL_SERVICE_ID+"detail:";//ymcc:kill:detail:
    String KILL_ACTIVITY_COURSE_COUNT = APP_ID+KILL_SERVICE_ID + "coursecount:";//ymcc:kill:coursecount:1:12
    Object KILL_ORDER = APP_ID+KILL_SERVICE_ID+"orders:";//ymcc:kill:activity:orders:123123123
}

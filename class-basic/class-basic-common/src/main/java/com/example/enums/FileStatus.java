package com.example.enums;

/**
 *    if(row.fileStatus == 0){
 *           return '待推流';
 *         }
 * 				if(row.fileStatus == 1){
 * 					return '推流中...';
 * 				                }
 * 				if(row.fileStatus == 2){
 * 					return '上传成功';
 *                }
 * 				if(row.fileStatus == 3){
 * 					return '上传失败';
 *                }
 * 				if(row.fileStatus == 4){
 * 					return '无需处理';
 *                }
 */
public enum FileStatus {
    TO_PUSH(0,"待推流"),
    PUSH_ING(1,"推流中"),
    PUSH_END(2,"推流完成"),
    PUSH_ERROR(3,"推流失败"),
    ;
    private Integer code;
    private String desc;

    FileStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

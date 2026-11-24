package com.mental.common;

public enum ResultCode {
    SUCCESS(200,"成功"),
    ERROR(500,"系统异常"),
    NO_LOGIN(401,"未登录"),
    NO_PERMISSION(403,"无权限");

    private int code;
    private String msg;

    ResultCode(int code,String msg){
        this.code=code;
        this.msg=msg;
    }

    public int getCode(){return code;}
    public String getMsg(){return msg;}
}
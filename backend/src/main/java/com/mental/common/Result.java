package com.mental.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(){
        return success(null);
    }

    public static <T> Result<T> success(T data){
        Result<T> r=new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String msg){
        Result<T> r=new Result<>();
        r.setCode(ResultCode.ERROR.getCode());
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> error(int code,String msg){
        Result<T> r=new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
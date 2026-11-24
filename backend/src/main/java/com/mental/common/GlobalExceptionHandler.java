package com.mental.common;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public Result handle(Exception e){
        e.printStackTrace();
        return Result.error(e.getMessage());
    }
}
package com.mental.controller;

import lombok.Data;

import java.util.Date;

@Data
public class UserRegisterVO {
    private String userAccount;
    private String userPassword;
    private String username;
    private String password;
    private String role;

    @Data
    public static class ExamRecordVO {
        private Long id;
        private Long scaleId;
        private String scaleName;
        private String scaleType;
        private Integer score;
        private String level;
        private String suggestion;
        private Date createTime;
    }
}

package com.mental.security;

import com.mental.common.Result;
import com.mental.common.ResultCode;

public class Access {
    public static boolean isAdmin(){
        return "ADMIN".equals(UserContext.getRole());
    }
    public static Result checkAdmin(){
        if(!isAdmin()) return Result.error(ResultCode.NO_PERMISSION.getCode(),"无管理员权限");
        return null;
    }

    // 老师权限
    public static boolean isTeacher(){
        return "TEACHER".equals(UserContext.getRole());
    }
    public static Result checkTeacher(){
        if(!isTeacher()) return Result.error(ResultCode.NO_PERMISSION.getCode(),"无教师权限");
        return null;
    }

    // 管理员或老师权限
    public static boolean isAdminOrTeacher(){
        return isAdmin() || isTeacher();
    }
    public static Result checkAdminOrTeacher(){
        if(!isAdminOrTeacher()) return Result.error(ResultCode.NO_PERMISSION.getCode(),"无管理员或教师权限");
        return null;
    }
}
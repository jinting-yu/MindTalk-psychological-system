package com.mental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;    // 用户名

    private String password;     // 密码

    @TableField("real_name")
    private String realName;     // 真实姓名（数据库字段是 real_name）

    private String phone;        // 手机号

    private String role;         // 角色: ADMIN, TEACHER, USER

    @TableField("create_time")
    private Date createTime;     // 创建时间

    // 以下字段如果数据库没有，需要删除
    // private String userAccount;
    // private String userName;
    // private String userProfile;
    // private String email;
    // private Date updateTime;
}
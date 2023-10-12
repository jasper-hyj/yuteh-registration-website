package com.yuteh.register.user.model;

import lombok.Data;

import java.sql.Timestamp;

// User of the website
@Data
public class User {
    private String email;
    private String name;
    private String role;
    private boolean validity;
    private Timestamp createTime;
    private String createUser;
    private Timestamp updateTime;
    private String updateUser;
}

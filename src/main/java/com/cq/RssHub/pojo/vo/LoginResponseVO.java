package com.cq.RssHub.pojo.vo;

import java.io.Serializable;

/**
 * 登录响应VO
 */
public class LoginResponseVO implements Serializable {
    private String token;
    private UserInfoVO user;

    public LoginResponseVO() {
    }

    public LoginResponseVO(String token, UserInfoVO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoVO getUser() {
        return user;
    }

    public void setUser(UserInfoVO user) {
        this.user = user;
    }
} 
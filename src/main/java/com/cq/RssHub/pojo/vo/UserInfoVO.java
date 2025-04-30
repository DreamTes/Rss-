package com.cq.RssHub.pojo.vo;

import java.io.Serializable;

/**
 * 用户信息VO
 */
public class UserInfoVO implements Serializable {
    private Integer id;
    private String username;
    private String email;

    public UserInfoVO() {
    }

    public UserInfoVO(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 
package com.cq.RssHub.service;

import com.cq.RssHub.pojo.DTO.UserDTO;
import com.cq.RssHub.pojo.User;

public interface UserService {


    /**
     * 注册用户
     * @param user
     * @return
     */
    User RegisterUser(UserDTO user);

    /**
     *根据用户名更新用户
     * @param username
     * @param user
     * @return
     */
    User UpdateUser(String username, UserDTO user);


    /**
     * 根据用户名获取用户
     * @param username
     * @return User
     */
    User GetUserByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username
     * @return true:存在 false 不存在
     */
    boolean UserByUsername(String username);

    /**
     * 根据用户名修改密码
     * @param username
     * @return User
     */
    User UpdateUserPassword(String username, UserDTO userDTO);
}

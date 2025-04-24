package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.UserMapper;
import com.cq.RssHub.pojo.DTO.UserDTO;
import com.cq.RssHub.pojo.User;
import com.cq.RssHub.service.UserService;
import com.cq.RssHub.utils.MD5Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;


    @Override
    public User RegisterUser(UserDTO user) {
        // 密码加密
        String md5 =MD5Util.MD5(user.getPassword()) ;
        user.setPassword(md5);
        User userDO = new User();
        BeanUtils.copyProperties(user, userDO);
        userMapper.addUser(userDO);
        return userDO;
    }

    @Override
    public User UpdateUser(String username, UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user); //这个是Spring提供的一个工具类，可以自动将userDTO中的属性值赋给userDO
        userMapper.updateUser(username,user);
        return user;
    }

    @Override
    public User UpdateUserPassword(String username, UserDTO userDTO) {

        User user = new User();
        BeanUtils.copyProperties(userDTO, user); //这个是Spring提供的一个工具类，可以自动将userDTO中的属性值赋给userDO
        userMapper.updateUserPassword(username,user);
        return user;
    }

    @Override
    public User GetUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public boolean UserByUsername(String username) {
        return userMapper.getUserByUsername(username) != null; // 判断用户是否存在
    }


}

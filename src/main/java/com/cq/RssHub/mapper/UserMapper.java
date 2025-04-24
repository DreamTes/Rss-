package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
     // 添加用户
     int addUser(User user);
     // 按照用户名更新用户
     void updateUser(@Param("username") String username, User user);
     // 按照用户名查询用户
     User getUserByUsername(@Param("username") String username);

     void updateUserPassword(String username, User user);
}

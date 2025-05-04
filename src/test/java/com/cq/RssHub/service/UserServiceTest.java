package com.cq.RssHub.service;

import com.cq.RssHub.mapper.UserMapper;
import com.cq.RssHub.pojo.DTO.UserDTO;
import com.cq.RssHub.pojo.User;
import com.cq.RssHub.service.impl.UserServiceImpl;
import com.cq.RssHub.utils.MD5Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        // 准备测试数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password123");
        userDTO.setEmail("test@example.com");

        User expectedUser = new User();
        BeanUtils.copyProperties(userDTO, expectedUser);
        expectedUser.setPassword(MD5Util.MD5("password123"));

        // 模拟userMapper的行为
        when(userMapper.addUser(any(User.class))).thenReturn(1);

        // 调用被测试方法
        User result = userService.RegisterUser(userDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(MD5Util.MD5("password123"), result.getPassword());
        assertEquals("test@example.com", result.getEmail());

        // 验证userMapper的addUser方法被调用
        verify(userMapper, times(1)).addUser(any(User.class));
    }

    @Test
    public void testUpdateUser_Success() {
        // 准备测试数据
        String username = "existinguser";
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("updated@example.com");

        User expectedUser = new User();
        BeanUtils.copyProperties(userDTO, expectedUser);

        // 模拟userMapper的行为 - 根据接口定义，updateUser方法是void类型
        doNothing().when(userMapper).updateUser(eq(username), any(User.class));

        // 调用被测试方法
        User result = userService.UpdateUser(username, userDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());

        // 验证userMapper的updateUser方法被调用
        verify(userMapper, times(1)).updateUser(eq(username), any(User.class));
    }

    @Test
    public void testGetUserByUsername_Success() {
        // 准备测试数据
        String username = "existinguser";
        User expectedUser = new User();
        expectedUser.setUsername(username);
        expectedUser.setPassword(MD5Util.MD5("password123"));
        expectedUser.setEmail("existing@example.com");

        // 模拟userMapper的行为
        when(userMapper.getUserByUsername(username)).thenReturn(expectedUser);

        // 调用被测试方法
        User result = userService.GetUserByUsername(username);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("existing@example.com", result.getEmail());

        // 验证userMapper的getUserByUsername方法被调用
        verify(userMapper, times(1)).getUserByUsername(username);
    }

    @Test
    public void testGetUserByUsername_UserNotFound() {
        // 准备测试数据
        String username = "nonexistentuser";

        // 模拟userMapper的行为
        when(userMapper.getUserByUsername(username)).thenReturn(null);

        // 调用被测试方法
        User result = userService.GetUserByUsername(username);

        // 验证结果
        assertNull(result);

        // 验证userMapper的getUserByUsername方法被调用
        verify(userMapper, times(1)).getUserByUsername(username);
    }

    @Test
    public void testUserByUsername_UserExists() {
        // 准备测试数据
        String username = "existinguser";
        User user = new User();
        user.setUsername(username);

        // 模拟userMapper的行为
        when(userMapper.getUserByUsername(username)).thenReturn(user);

        // 调用被测试方法
        boolean result = userService.UserByUsername(username);

        // 验证结果
        assertTrue(result);

        // 验证userMapper的getUserByUsername方法被调用
        verify(userMapper, times(1)).getUserByUsername(username);
    }

    @Test
    public void testUserByUsername_UserNotExists() {
        // 准备测试数据
        String username = "nonexistentuser";

        // 模拟userMapper的行为
        when(userMapper.getUserByUsername(username)).thenReturn(null);

        // 调用被测试方法
        boolean result = userService.UserByUsername(username);

        // 验证结果
        assertFalse(result);

        // 验证userMapper的getUserByUsername方法被调用
        verify(userMapper, times(1)).getUserByUsername(username);
    }

    @Test
    public void testUpdateUserPassword_Success() {
        // 准备测试数据
        String username = "existinguser";
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("newpassword123");

        User expectedUser = new User();
        BeanUtils.copyProperties(userDTO, expectedUser);

        // 模拟userMapper的行为 - 根据接口定义，updateUserPassword方法是void类型
        doNothing().when(userMapper).updateUserPassword(eq(username), any(User.class));

        // 调用被测试方法
        User result = userService.UpdateUserPassword(username, userDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("newpassword123", result.getPassword());

        // 验证userMapper的updateUserPassword方法被调用
        verify(userMapper, times(1)).updateUserPassword(eq(username), any(User.class));
    }
} 
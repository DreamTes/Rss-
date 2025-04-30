package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.DTO.UserDTO;
import com.cq.RssHub.pojo.LoginRequest;
import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.User;
import com.cq.RssHub.pojo.vo.LoginResponseVO;
import com.cq.RssHub.pojo.vo.UserInfoVO;
import com.cq.RssHub.service.UserService;
import com.cq.RssHub.utils.JwtUtil;
import com.cq.RssHub.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    JwtUtil jwtUtil;
    //增加用户（注册）
//    @RequestBody
//    作用: 用于将 HTTP 请求体中的 JSON 数据自动转换为 Java 对象。
//    使用场景: 当客户端发送一个 JSON 格式的请求体时，Spring 会自动将其映射到指定的对象。
//    @Validated
//    作用: 用于触发校验机制，通常与 JSR 303（Bean Validation）规范结合使用。它会在方法参数或类字段上应用校验规则。
//    使用场景: 需要对传入的数据进行合法性校验时。
    @PostMapping("/register")
    public ResponseMessage<User> UserAdd(@Validated @RequestBody UserDTO user) {
        //查询用户名是否存在
        if(!userService.UserByUsername(user.getUsername())){
            User userDO = userService.RegisterUser(user);
            return ResponseMessage.success(userDO);
        }else {
            return ResponseMessage.error("用户名已存在");
        }
    }

    //登录
    @PostMapping("/login")
    public ResponseMessage<?> UserLogin(@Validated @RequestBody LoginRequest loginRequest) {
        if(userService.UserByUsername(loginRequest.getUsername())){
            User loginUser = userService.GetUserByUsername(loginRequest.getUsername());
            if(loginUser.getPassword().equals(MD5Util.MD5(loginRequest.getPassword()))){
                String token = jwtUtil.generateToken(loginUser.getUsername());//JWT生成token

                // 创建UserInfoVO对象
                UserInfoVO userInfoVO = new UserInfoVO(
                    loginUser.getId(),
                    loginUser.getUsername(),
                    loginUser.getEmail()
                );
                
                // 创建LoginResponseVO对象
                LoginResponseVO responseVO = new LoginResponseVO(token, userInfoVO);

                return ResponseMessage.success("登录成功", responseVO); //统一的返回格式
            }
            else {
                return ResponseMessage.error("密码错误");
            }
        }else {
            return ResponseMessage.error("用户名不存在,请注册");
        }
    }

    //验证当前用户是否登录
    @GetMapping("/check")
    public ResponseMessage<?> UserCheck(@RequestHeader("Authorization") String token) {
        try {
            // 从token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            if(username != null) {
                // 根据用户名获取用户信息
                User user = userService.GetUserByUsername(username);

                if(user != null) {
                    // 创建用户信息对象
                    UserInfoVO userInfoVO = new UserInfoVO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                    );

                    // 返回成功响应，包含用户信息
                    return ResponseMessage.success("已登录", userInfoVO);
                }
            }

            // 如果未能获取到用户信息，返回未登录状态
            return ResponseMessage.unauthorized("未登录");
        } catch (Exception e) {
            // 捕获可能出现的异常，如token解析失败等
            return ResponseMessage.unauthorized("登录已过期或无效");
        }
    }

    //用户退出登录
    //用户退出登录
    @PostMapping("/logout")
    public ResponseMessage<?> UserLogout(@RequestHeader("Authorization") String token) {
        try {
            // 从token中获取用户名，确认token有效
//            String username = jwtUtil.getUsernameFromToken(token);
//
//            if(username != null) {
//                // 使token失效（加入黑名单）
//                boolean invalidated = jwtUtil.invalidateToken(token);
//
//                if(invalidated) {
//                    return ResponseMessage.success("退出成功");
//                } else {
//                    return ResponseMessage.error("退出失败：无法使令牌失效");
//                }
//            } else {
//                return ResponseMessage.error("无效的登录信息");
//            }
            return ResponseMessage.success("退出成功");
        } catch (Exception e) {
            return ResponseMessage.error("退出失败: " + e.getMessage());
        }
    }
//    获取用户个人资料
    @GetMapping("/profile")
    public ResponseMessage<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                User user = userService.GetUserByUsername(username);
                // 构建返回数据，不包含敏感信息
                UserInfoVO userInfoVO = new UserInfoVO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
                );

                return ResponseMessage.success("获取成功", userInfoVO);
            }
            return ResponseMessage.unauthorized("未登录");
        } catch (Exception e) {
            return ResponseMessage.error("获取个人资料失败: " + e.getMessage());
        }
    }
//    更新用户个人资料
    @PutMapping("/profile")
    public ResponseMessage<?> updateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserDTO userDTO) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {

                User updated = userService.UpdateUser(username,userDTO);

                // 构建返回数据
                UserInfoVO userInfoVO = new UserInfoVO(
                    updated.getId(),
                    updated.getUsername(),
                    updated.getEmail()
                );

                return ResponseMessage.success("更新成功", userInfoVO);
            }
            return ResponseMessage.unauthorized("未登录");
        } catch (Exception e) {
            return ResponseMessage.error("更新个人资料失败: " + e.getMessage());
        }
    }

    //修改密码
    @PutMapping("/password")
    public ResponseMessage<?> updateUserPassword(
            @RequestHeader("Authorization") String token,
            @RequestBody UserDTO userDTO) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                User user = userService.GetUserByUsername(username);//获取用户信息
                String password = user.getPassword(); //用户当前密码

                String md5 =MD5Util.MD5(userDTO.getPassword()) ;
                userDTO.setPassword(md5);
                if(password.equals(userDTO.getPassword())){
                    return ResponseMessage.error("新密码与旧密码相同");
                }else{
                    userService.UpdateUserPassword(username,userDTO);
                    return ResponseMessage.success("更新成功");
                }
            }
            return ResponseMessage.unauthorized("未登录");
        } catch (Exception e) {
            return ResponseMessage.error("更新密码失败: " + e.getMessage());
        }
    }
//    @PathVariable
//    作用: 用于将请求 URL 中的动态参数绑定到方法参数上。
//    使用场景: 当需要从路径中提取参数时，例如 /user/{id} 中的 {id}。
    //根据用户名查询用户
    @GetMapping("username/{username}")
    public ResponseMessage<User> UserGetByUsername(@PathVariable String username) {
        if(userService.UserByUsername(username)){
            User userDO = userService.GetUserByUsername(username);
            return ResponseMessage.success(userDO);
        }else {
            return ResponseMessage.error("用户名不存在");
        }

    }

}

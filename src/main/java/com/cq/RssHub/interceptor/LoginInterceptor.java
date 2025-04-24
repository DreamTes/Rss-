package com.cq.RssHub.interceptor;

import com.cq.RssHub.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) { // 解决跨域问题
            return true;
        }
        String token = request.getHeader("Authorization");
        //令牌验证
        request.getHeader("Authorization");

        try {
            jwtUtil.getUsernameFromToken(token);
            //验证通过，放行
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            //验证不通过，拦截
            return false;
        }
    }
}

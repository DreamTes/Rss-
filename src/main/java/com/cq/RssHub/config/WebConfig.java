package com.cq.RssHub.config;

import com.cq.RssHub.interceptor.LoginInterceptor;
import com.cq.RssHub.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/user/login","/user/register","/user/logout")
                .excludePathPatterns(HttpMethod.OPTIONS.name());;

    }

    // CORS配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("CORS配置已加载");
        registry.addMapping("/**") // 允许所有请求路径
                .allowedOrigins("http://localhost:5173") // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 预检请求结果缓存时间
    }
}

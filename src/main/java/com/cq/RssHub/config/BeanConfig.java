package com.cq.RssHub.config;

import com.cq.RssHub.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}

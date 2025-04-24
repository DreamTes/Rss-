package com.cq.RssHub;

import com.cq.RssHub.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RssDemoApplicationTests {

    @Autowired
    private JwtUtil jwtUtil;



    private String username;
    private String token;
    @BeforeEach
    public void setup() {
        // 设置测试用户名
        username = "testuser@example.com";

        // 生成测试Token
       token = jwtUtil.generateToken(username);
    }
    @Test
    public void testTokenGeneration() {
        System.out.println(token);
        // 测试令牌生成
        assertNotNull(token, "Token should not be null");
        assertTrue(token.length() > 0, "Token should have length");
    }
    @Test
    public void testUsernameExtraction() {
        // 测试从令牌中提取用户名
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertEquals(username, extractedUsername, "Extracted username should match original");
    }

}

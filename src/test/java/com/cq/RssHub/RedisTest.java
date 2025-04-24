package com.cq.RssHub;

import com.cq.RssHub.pojo.UserRedisTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testRedis() {
        // 测试redis
        redisTemplate.opsForValue().set("name", "tcq");
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
    //json 序列化
    private  static  final ObjectMapper Mapper = new ObjectMapper();
    @Test
    public void testRedis2() throws JsonProcessingException {
        // 测试redis
        UserRedisTest userRedisTest = new UserRedisTest("张三",12);
        // 序列化
        String json = Mapper.writeValueAsString(userRedisTest);
        // 写入redis
        stringRedisTemplate.opsForValue().set("user:100",json);
        // 读取redis
        String val = stringRedisTemplate.opsForValue().get("user:100");
        // 反序列化
        UserRedisTest user = Mapper.readValue(val, UserRedisTest.class);
        System.out.println("user1"+user);

    }
}

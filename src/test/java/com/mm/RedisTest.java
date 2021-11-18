package com.mm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void val() {
        redisTemplate.opsForValue().set("abc", "123");
        System.out.println(redisTemplate.opsForValue().get("abc"));
    }


    @Test
    public void send() {
        redisTemplate.convertAndSend("aa", "hello 你好");
    }



}

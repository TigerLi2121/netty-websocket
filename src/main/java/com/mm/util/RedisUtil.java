package com.mm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis Util
 *
 * @author lwl
 */
@Slf4j
@Component
public class RedisUtil {

    public static RedisTemplate redisTemplate;

    public RedisUtil(RedisTemplate redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    public static Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    public static void set(String key, String val, Long expire) {
        if (expire == null) {
            redisTemplate.opsForValue().set(key, val);
        } else {
            redisTemplate.opsForValue().set(key, val, expire, TimeUnit.SECONDS);
        }
    }

    public static void set(String key, String val) {
        set(key, val, null);
    }

    public static Optional<String> get(String key) {
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(key));
    }

    public static void hashPut(String key, String hk, String val) {
        redisTemplate.opsForHash().put(key, hk, val);
    }

    public static Optional<String> hashGet(String key, String hk) {
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(key, hk));
    }

    public static Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }
}

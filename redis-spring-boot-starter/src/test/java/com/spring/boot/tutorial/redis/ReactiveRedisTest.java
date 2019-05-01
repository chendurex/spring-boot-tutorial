package com.spring.boot.tutorial.redis;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.time.Duration;

/**
 * @author cheny.huang
 * @date 2018-08-18 19:23.
 */
public class ReactiveRedisTest extends BaseApplication {
    @Autowired
    private ReactiveRedisTemplate<String, String> template;
    @Test
    public void testReactiveRedis() {
        String k = "k";
        String v = "v";
        template.opsForValue().set(k, v, Duration.ofSeconds(10)).block();
        Assert.assertTrue(v.equals(template.opsForValue().get(k).block()));
    }
}

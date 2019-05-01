package com.spring.boot.tutorial.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * @author cheny.huang
 * @date 2018-08-14 14:46.
 */
public class StringRedisTemplateTest extends BaseApplication {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource(name = "redisTemplate")
    private RedisTemplate<String, RedisUser> redisTemplate;
    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Integer> intRedisTemplate;
    private static final String KEY = "redis:string:redis:template";
    @Test
    public void stringTest() {
        boolean suc = stringRedisTemplate.opsForValue().setIfAbsent(KEY, KEY);
        assertTrue(suc);
        assertEquals(KEY, stringRedisTemplate.opsForValue().get(KEY));
        assertTrue(stringRedisTemplate.delete(KEY));
        assertNull(stringRedisTemplate.opsForValue().get(KEY));
    }

    @Test
    public void objectTest() {
        boolean suc = redisTemplate.opsForValue().setIfAbsent(KEY, RedisUser.builder().name("hello").age(1).build());
        assertTrue(suc);
        assertEquals("hello", redisTemplate.opsForValue().get(KEY).getName());
        assertTrue(redisTemplate.delete(KEY));
        assertNull(redisTemplate.opsForValue().get(KEY));
    }

    @Test
    public void testIntOperation() {
        ValueOperations<String, Integer> valueOperations = intRedisTemplate.opsForValue();
        intRedisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        valueOperations.set(KEY, 1);
        assertEquals((int)valueOperations.get(KEY), 1);
        assertEquals((long)valueOperations.increment(KEY, 1), 2);
    }
}

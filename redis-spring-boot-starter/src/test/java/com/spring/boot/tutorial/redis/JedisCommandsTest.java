package com.spring.boot.tutorial.redis;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCommands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cheny.huang
 * @date 2018-08-14 10:02.
 */

public class JedisCommandsTest extends BaseApplication {
    @Autowired
    private JedisCommands jedisCommands;
    private static final String KEY = "jedis:commands:test:key";
    @Test
    public void testString() {
        for (int i=0;i<30;i++) {
            String k = KEY + i;
            jedisCommands.setex(k, 20, k);
            Assert.assertTrue(k.equals(jedisCommands.get(k)));
        }
    }

    @Test
    public void testIncrease() {
        jedisCommands.set(KEY, "1");
        assertEquals(jedisCommands.get(KEY), "1");
        assertEquals((long)jedisCommands.incr(KEY), 2);
        assertTrue(jedisCommands.del(KEY) > 0);
    }
}

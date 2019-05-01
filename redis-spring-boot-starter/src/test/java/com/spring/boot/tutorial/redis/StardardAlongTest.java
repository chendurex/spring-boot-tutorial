package com.spring.boot.tutorial.redis;

import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author cheny.huang
 * @date 2018-08-27 13:56.
 */
public class StardardAlongTest {
    @Test
    public void setAdd() {
        Jedis jedis = new Jedis("192.168.2.56", 6382);
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<1000;i++) {
            sb.append("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
        String v = sb.toString();

        for (int i = 0;i < 1000;i++) {
            try {
                jedis.set("sba14" + i, v);
            } catch (Exception e) {
                // ignore
            }

        }
    }
}

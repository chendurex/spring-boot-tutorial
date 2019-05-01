package com.spring.boot.tutorial.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;

import static org.junit.Assert.assertFalse;

/**
 * @author cheny.huang
 * @date 2019-01-17 15:51.
 */
public class RedisNodeUtilTest extends BaseApplication {
    @Autowired
    private JedisCluster cluster;

    @Test
    public void testMasterNodes() {
        assertFalse(RedisNodeUtils.fetchMaster(cluster).isEmpty());
        assertFalse(RedisNodeUtils.fetchSlave(cluster).isEmpty());
    }
}

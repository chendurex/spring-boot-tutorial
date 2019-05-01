package com.spring.boot.tutorial.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cheny.huang
 * @date 2019-01-17 11:32.
 */
public final class RedisNodeUtils {
    private static String nodes(JedisCluster cluster) {
        try (Jedis jedis = cluster.getClusterNodes().values().iterator().next().getResource()) {
            return jedis.clusterNodes();
        }
    }

    public static List<String> fetchMaster(JedisCluster cluster) {
        return fetch(cluster, "master");
    }

    public static List<String> fetchSlave(JedisCluster cluster) {
        return fetch(cluster, "slave");
    }

    private static List<String> fetch(JedisCluster cluster, String key) {
        String nodes = nodes(cluster);
        List<String> fetch = new ArrayList<>();
        for (String s : nodes.split("\n")) {
            if (s.contains(key)) {
                fetch.add(s.substring(s.indexOf(" ")+1, s.indexOf("@")));
            }
        }
        return fetch;
    }
}

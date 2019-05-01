package com.spring.boot.tutorial.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Collections;

/**
 * @author cheny.huang
 * @date 2018-12-04 17:12.
 */
@Slf4j
class FaultyClusterDiscovery {
    /**
     * jedis在发现可用节点时，如果节点有设置密码的话，并不会选择重试其它的节点，而是直接抛出异常
     *
     * {@link redis.clients.jedis.JedisClusterConnectionHandler#initializeSlotsCache}
     * @param clusterConfig
     * @return
     */
    RedisClusterConfiguration discoveryAvailableNodes(RedisClusterConfiguration clusterConfig) {
        String password = getPassword(clusterConfig);
        if (password != null) {
            for (RedisNode node : clusterConfig.getClusterNodes()) {
                try (Jedis jedis = new Jedis(node.getHost(), node.getPort())) {
                    jedis.auth(getPassword(clusterConfig));
                    clusterConfig.setClusterNodes(Collections.singleton(new RedisNode(node.getHost(), node.getPort())));
                    log.info("检测到可用节点，重新设置redis配置节点为单节点：host:{}, port:{}", node.getHost(), node.getPort());
                    break;
                } catch (JedisConnectionException e) {
                    log.warn("", e);
                }
            }
        }
        return clusterConfig;
    }

    private String getPassword(RedisClusterConfiguration clusterConfig) {
        return clusterConfig.getPassword().map(String::new).orElse(null);
    }
}

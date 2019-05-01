package com.spring.boot.tutorial.redis.scan;

import com.spring.boot.tutorial.redis.RedisNodeUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * @author cheny.huang
 * @date 2019-01-17 17:56.
 */
class AggressionScanOperation extends AbstractScanOperation {

    AggressionScanOperation(JedisCluster jedisCluster) {
        super(jedisCluster);
    }

    /**
     * 循环索引每个服务端的数据，把每个服务端的数据都索引按照limit限制索引出来
     * 然后再将数据进行聚合操作，如果还存在未索引完毕的数据，那么返回所有服务器中索引值最小的那个，方便下次重复索引
     */
    @Override
    AggressionResult internalScan(String pattern, int limit, AggressionResult aggressionResult) {
        AggressionResult scanResult = new AggressionResult();
        // 确认集群中存在数据
        if (aggressionResult.hasNext()) {
            for (String s : RedisNodeUtils.fetchMaster(jedisCluster())) {
                // 判断单个服务端是否存在索引数据
                if (aggressionResult.hasNext(s)) {
                    try(Jedis jedis = jedisCluster().getClusterNodes().get(s).getResource()) {
                        ScanResult<String> result = jedis.scan(aggressionResult.nextSlot(s), new ScanParams().match(pattern).count(limit));
                        scanResult.addResult(result.getResult()).addSlot(s, result.getStringCursor());
                    }
                } else {
                    // 如果节点存在，但是数据已经检索完毕，则继续把原始数据存储，防止下次使用时，数据为空，默认是初始化的数据
                    scanResult.addSlot(s, aggressionResult.nextSlot(s));
                }
            }
            return scanResult;
        }
        return new AggressionResult().nothing();
    }
}

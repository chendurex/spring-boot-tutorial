package com.spring.boot.tutorial.redis.scan;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * @author cheny.huang
 * @date 2019-01-17 17:03.
 */
class HashTagScanOperation extends AbstractScanOperation {
    private static final String CLUSTER_HAST_TAG_SLOT = "cluster-hash-tag";
    HashTagScanOperation(JedisCluster jedisCluster) {
        super(jedisCluster);
    }

    @Override
    AggressionResult internalScan(String pattern, int limit, AggressionResult aggressionResult) {
        if (aggressionResult.hasNext(CLUSTER_HAST_TAG_SLOT)) {
            ScanResult<String> scanResult = jedisCluster().scan(
                    aggressionResult.nextSlot(CLUSTER_HAST_TAG_SLOT), new ScanParams().match(pattern).count(limit));
            return new AggressionResult().addResult(scanResult.getResult()).addSlot(CLUSTER_HAST_TAG_SLOT, scanResult.getStringCursor());
        }
        return new AggressionResult().nothing();
    }
}

package com.spring.boot.tutorial.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author cheny.huang
 * @date 2019-01-17 15:49.
 */
public class ScanCommandTest extends BaseApplication {
    @Autowired
    private JedisCluster jedisCluster;
    private static final String PREFIX="scan-command-test-";
    private static final String PREFIX_PATTERN="scan-command-test-*";
    private static final String HASH_TAG_PREFIX="{hashtag-scan-command-test}-";
    private static final String HASH_TAG_PATTERN="{hashtag-scan-command-test}-*";
    @Test
    public void testClusterScan() {
        for (int i=0;i<100;i++) {
            jedisCluster.set(PREFIX+i,PREFIX);
        }
        List<String> keys = new ArrayList<>();
        for (String s : RedisNodeUtils.fetchMaster(jedisCluster)) {
            ScanResult<String> result = jedisCluster.getClusterNodes().get(s).getResource()
                    .scan("0", new ScanParams().match(PREFIX_PATTERN).count(1000));
            keys.addAll(result.getResult());
        }
        assertEquals(keys.size(), 100);
    }

    @Test
    public void testScan() {
        for (int i=0;i<100;i++) {
            jedisCluster.set(HASH_TAG_PREFIX+i,PREFIX);
        }
        assertEquals(100, jedisCluster.scan("0", new ScanParams().match(HASH_TAG_PATTERN).count(1000)).getResult().size());

        ScanResult<String> scanResult = jedisCluster.scan("0", new ScanParams().match(HASH_TAG_PATTERN).count(10));
        List<String> keys = new ArrayList<>();
        keys.addAll(scanResult.getResult());
        while (!"0".equals(scanResult.getStringCursor())) {
            scanResult = jedisCluster.scan(scanResult.getStringCursor(), new ScanParams().match(HASH_TAG_PATTERN).count(10));
            keys.addAll(scanResult.getResult());
        }
        assertEquals(keys.size(), 100);
    }

    @Test
    public void clear() {
        String key = "dsp_ps_fls_uid_to_company_*\n" +
                "dsp_ps_fls_wholeCode_to_propertyName_*\n" +
                "dsp_ps_fls_userId_to_Account_*\n" +
                "dsp_ps_fls_userId_to_Member_*\n" +
                "dsp_ps_fls_companyId_to_DecInfo_*";
        for (String k : key.split("\n")) {
            List<String> keys = new ArrayList<>();
            for (String s : RedisNodeUtils.fetchMaster(jedisCluster)) {
                ScanResult<String> result = jedisCluster.getClusterNodes().get(s).getResource()
                        .scan("0", new ScanParams().match(k).count(100000));
                keys.addAll(result.getResult());
            }
            keys.forEach(s->jedisCluster.del(s));
        }

    }
}

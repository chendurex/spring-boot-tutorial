package com.spring.boot.tutorial.redis;

import com.spring.boot.tutorial.redis.operation.AbstractDefaultOperation;
import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cheny.huang
 * @date 2019-01-18 11:09.
 */
public class ClusterScanOperationTest extends BaseApplication {
    @Autowired
    private ClusterScanOperation scanOperation;
    @Autowired
    private AbstractDefaultOperation<String> defaultOperation;
    private static final String PREFIX = "cluster:scan:operation:";
    private static final String PREFIX_HASH_TAG = "{cluster:scan:operation}:";
    private static final String PREFIX_PATTERN = "cluster:scan:operation:*";
    private static final String PREFIX_HASH_TAG_PATTERN = "{cluster:scan:operation}:*";
    @Test
    public void testHashTagScan() {
        testScan(PREFIX_HASH_TAG, PREFIX_HASH_TAG_PATTERN);
    }

    @Test
    public void testClusterScan() {
        testScan(PREFIX, PREFIX_PATTERN);
    }

    private void testScan(String key, String pattern) {
        for (int i=0;i<1000;i++) {
            defaultOperation.set(key+i, PREFIX);
        }
        ClusterScanOperation.AggressionResult scanResult = scanOperation.scan(pattern, 10000);
        assertEquals(1000, scanResult.getResult().size());

        scanResult = scanOperation.scan(pattern, 10);
        // 如果是cluster模式下，可能是多个服务端组合出来的数据，所以可能大于10
        assertTrue(scanResult.getResult().size() < 100);
        List<String> keys = new LinkedList<>();
        keys.addAll(scanResult.getResult());
        while (scanResult.hasNext()) {
            scanResult = scanOperation.scan(pattern, 10, scanResult);
            keys.addAll(scanResult.getResult());
        }
        assertEquals(1000, keys.size());

        keys.forEach(defaultOperation::delete);

        scanResult = scanOperation.scan(pattern, 1000);
        assertEquals(0, scanResult.getResult().size());
    }
}

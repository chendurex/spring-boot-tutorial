package com.spring.boot.tutorial.redis;

import com.spring.boot.tutorial.redis.operation.AbstractPrefixOperation;
import com.spring.boot.tutorial.redis.operation.NamespaceOperation;
import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import com.spring.boot.tutorial.redis.operation.AbstractDefaultOperation;
import com.spring.boot.tutorial.redis.operation.AbstractHashTagOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author cheny.huang
 * @date 2019-01-05 16:36.
 */
public class NamespaceOperationTest extends BaseApplication {
    // 缓存key自动带上前缀的操作，默认是spring.application.name，可以通过spring.application.redis.key.prefix修改
    @Autowired
    private AbstractPrefixOperation<String> prefixOperation;
    // 值为integer类型的操作，比如使用inc操作
    @Autowired
    private AbstractPrefixOperation<Integer> prefixOperationForInt;
    // 值为对象类型的操作
    @Autowired
    private AbstractPrefixOperation<RedisUser> userPrefixOperation;
    @Autowired
    private AbstractDefaultOperation<String> defaultOperation;
    @Autowired
    private AbstractDefaultOperation<Integer> defaultOperationForInt;
    @Autowired
    private AbstractDefaultOperation<RedisUser> userDefaultOperation;
    // 类似AbstractPrefixOperation等操作，但是key会加上一个槽位标致，槽位一致的则会落在同一个redis服务端，比如希望业务key都落在一个服务端
    @Autowired
    private AbstractHashTagOperation<String> hashTagOperation;
    @Autowired
    private AbstractHashTagOperation<Integer> hashTagOperationForInt;
    @Autowired
    private AbstractHashTagOperation<RedisUser> userhashTagOperation;
    private static final String KEY = "namespace:key:test";
    private static final String VALUE = "namespace:key:test";

    @Test
    public void testStringOper() {
        prefixOperation.set(KEY, VALUE);
        assertEquals(prefixOperation.get(KEY), VALUE);
        assertTrue(prefixOperation.delete(KEY));
        defaultOperation.set(KEY, VALUE);
        assertEquals(defaultOperation.get(KEY), VALUE);
        assertTrue(defaultOperation.delete(KEY));
        hashTagOperation.set(KEY, VALUE);
        assertEquals(hashTagOperation.get(KEY), VALUE);
        assertTrue(hashTagOperation.delete(KEY));
    }

    @Test
    public void testPrefixStringOper() throws InterruptedException {
        fundamentalOperationTest(prefixOperation);
        intOperationTest(prefixOperationForInt);
        userOperationTest(userPrefixOperation);
        testClusterScan(prefixOperation, "cluster:prefix:scan", "cluster:prefix:scan*");
    }

    @Test
    public void testDefaultStringOper() throws InterruptedException {
        fundamentalOperationTest(defaultOperation);
        intOperationTest(defaultOperationForInt);
        userOperationTest(userDefaultOperation);
        testClusterScan(defaultOperation, "cluster:default:scan", "cluster:default:scan*");
    }

    @Test
    public void testHashTagStringOper() throws InterruptedException {
        fundamentalOperationTest(hashTagOperation);
        intOperationTest(hashTagOperationForInt);
        userOperationTest(userhashTagOperation);
        testClusterScan(hashTagOperation, "cluster:hashtag:scan", "cluster:hashtag:scan*");
    }

    private void intOperationTest(NamespaceOperation<Integer> operation) {
        operation.set(KEY, 1);
        assertEquals((int)operation.get(KEY), 1);
        assertEquals((long)operation.increment(KEY, 1), 2);
        assertEquals(operation.increment(KEY, 2.00), 4.00, 1);
        assertTrue(operation.delete(KEY));
    }

    private void userOperationTest(NamespaceOperation<RedisUser> operation) {
        // operation obj
        assertTrue(operation.setIfAbsent(KEY, RedisUser.builder().name("hello").age(1).build()));
        assertEquals("hello", operation.get(KEY).getName());
        assertTrue(operation.delete(KEY));
        assertNull(operation.get(KEY));
    }

    private void fundamentalOperationTest(NamespaceOperation<String> operation) {
        // fundamental oper
        operation.set(KEY, VALUE);
        assertEquals(operation.get(KEY), VALUE);
        assertFalse(operation.setIfAbsent(KEY, VALUE));
        assertEquals((long)operation.getExpire(KEY), -1);
        assertTrue(operation.delete(KEY));
        // batch delete
        operation.set(KEY+1, VALUE);
        operation.set(KEY+2, VALUE);
        operation.set(KEY+3, VALUE);
        assertEquals((long)operation.delete(Arrays.asList(KEY+1, KEY+2, KEY+3)), 3);

        // timeout oper
        operation.set(KEY, VALUE, 3, TimeUnit.SECONDS);
        assertEquals(operation.get(KEY), VALUE);
        long expired = operation.getExpire(KEY, TimeUnit.SECONDS);
        assertTrue(expired>=2 || expired<=1);
        assertTrue(operation.expire(KEY, 5, TimeUnit.SECONDS));
        expired = operation.getExpire(KEY, TimeUnit.SECONDS);
        assertTrue(expired>=3 || expired<=5);
        assertTrue(operation.expireAt(KEY, new Date(System.currentTimeMillis() + 10_1000)));
        expired = operation.getExpire(KEY, TimeUnit.SECONDS);
        assertTrue(expired>=7 || expired<=10);
        // append oper 因为序列化的原因，所以不能直接判定到底增加了多少内容
        assertTrue((long)operation.append(KEY, "a") > VALUE.length());
        try {
            // replace oper
            // 因为序列化原因，实际存储的内容并非与原始内容保持一致，所以无法通过字符串上下界限操作内容
            // 如果需要替换操作，请直接全值替换
            operation.set(KEY, "vvvv", 0);
            assertEquals(operation.get(KEY, 0, 3), "vvvv");
            assertFalse(true);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    private void testClusterScan(NamespaceOperation<String> operation, String key, String pattern) {
        for (int i=0;i<100;i++) {
            operation.set(key+i, key);
        }
        ClusterScanOperation.AggressionResult scanResult = operation.scan(pattern, 1000);
        assertEquals(100, scanResult.getResult().size());

        scanResult = operation.scan(pattern, 10);
        // 如果是cluster模式下，可能是多个服务端组合出来的数据，所以可能大于10
        assertTrue(scanResult.getResult().size() < 100);
        List<String> keys = new LinkedList<>();
        keys.addAll(scanResult.getResult());
        while (scanResult.hasNext()) {
            scanResult = operation.scan(pattern, 10, scanResult);
            keys.addAll(scanResult.getResult());
        }
        assertEquals(100, keys.size());
        keys.forEach(operation::delete);

        scanResult = operation.scan(pattern, 1000);
        assertEquals(0, scanResult.getResult().size());
    }
}

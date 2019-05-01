package com.spring.boot.tutorial.redis.operation;

import com.spring.boot.tutorial.redis.scan.ClusterScanOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 按照一定的前缀或者hashtag存储key
 * 如果有前缀并且hashtag=true，那么存储的key为{prefix}:key
 * 如果有前缀但是hashtag=false，那么存储的key为prefix:key
 * 如果没有前缀，那么存储的key为原始key，而hashtag无任何效果
 * @author cheny.huang
 * @date 2019-01-05 15:09.
 */
@Slf4j
abstract class AbstractNamespaceOperation<V> implements NamespaceOperation<V> {
    private ValueOperations<String,V> operations;
    private RedisTemplate<String, V> redisTemplate;
    private ClusterScanOperation clusterScanOperation;
    private final String prefix;
    private final boolean hashtag;
    protected AbstractNamespaceOperation(RedisTemplate<String, V> redisTemplate
            , ClusterScanOperation clusterScanOperation, String prefix, boolean hashtag) {
        this.clusterScanOperation = clusterScanOperation;
        this.redisTemplate = redisTemplate;
        this.operations = redisTemplate.opsForValue();
        this.prefix = prefix;
        this.hashtag = hashtag;
    }

    protected AbstractNamespaceOperation(RedisTemplate<String, V> operations,ClusterScanOperation clusterScanOperation, String prefix) {
        this(operations, clusterScanOperation, prefix, false);
    }


    private String wrap(String key) {
        if (prefix != null && !prefix.isEmpty()) {
            String newKey = hashtag ? "{" + prefix + "}:" + key : prefix + ":" + key;
            log.debug("origin key is :{}, wrapped key is :{}", key, newKey);
            return newKey;
        }
        return key;
    }

    private String unwrap(String key) {
        if (prefix != null && !prefix.isEmpty()) {
            String keyword = hashtag ? "{" + prefix + "}:" : prefix + ":";
            String newKey = key.replace(keyword, "");
            log.debug("origin key is :{}, unwrapped key is :{}", key, newKey);
            return newKey;
        }
        return key;
    }

    private Map<String,V> wrap(Map<? extends String, ? extends V> map) {
        return map.entrySet().stream().collect(Collectors.toMap(e->wrap(e.getKey()), Map.Entry::getValue));
    }

    private Collection<String> wrap(Collection<String> collections) {
        return collections.stream().map(this::wrap).collect(Collectors.toList());
    }

    @Override
    public ClusterScanOperation.AggressionResult scan(String pattern, int limit) {
        return scan(pattern, limit, new ClusterScanOperation.AggressionResult());
    }

    @Override
    public ClusterScanOperation.AggressionResult scan(String pattern, int limit, ClusterScanOperation.AggressionResult aggressionResult) {
        Objects.requireNonNull(pattern);
        if (!pattern.contains("*")) {
            throw new IllegalArgumentException("匹配模式请添加*，而且仅支持在字符串最后面追加");
        }
        String wrapper = wrap(pattern.substring(0, pattern.lastIndexOf("*"))) + "*";
        ClusterScanOperation.AggressionResult result = clusterScanOperation.scan(wrapper, limit, aggressionResult);
        List<String> converted = result.getResult().stream().map(this::unwrap).collect(Collectors.toList());
        result.getResult().clear();
        result.getResult().addAll(converted);
        return result;
    }

    @Override
    public void set(String key, V value) {
        operations.set(wrap(key), value);
    }

    @Override
    public void set(String key, V value, long timeout, TimeUnit unit) {
        operations.set(wrap(key), value, timeout, unit);
    }

    @Override
    public Boolean setIfAbsent(String key, V value) {
        return operations.setIfAbsent(wrap(key), value);
    }

    @Override
    public void multiSet(Map<? extends String, ? extends V> map) {
        operations.multiSet(wrap(map));
    }

    @Override
    public Boolean multiSetIfAbsent(Map<? extends String, ? extends V> map) {
        return operations.multiSetIfAbsent(wrap(map));
    }

    @Override
    public V get(Object key) {
        return operations.get(wrap(String.valueOf(key)));
    }

    @Override
    public V getAndSet(String key, V value) {
        return operations.getAndSet(wrap(key), value);
    }

    @Override
    public List<V> multiGet(Collection<String> keys) {
        return operations.multiGet(wrap(keys));
    }

    @Override
    public Long increment(String key, long delta) {
        return operations.increment(wrap(key), delta);
    }

    @Override
    public Double increment(String key, double delta) {
        return operations.increment(wrap(key), delta);
    }

    @Override
    public Integer append(String key, String value) {
        return operations.append(wrap(key), value);
    }

    @Override
    public String get(String key, long start, long end) {
        throw new UnsupportedOperationException("this method not supported due to serialization reason");
        //return operations.get(wrap(key), start, end);
    }

    @Override
    public void set(String key, V value, long offset) {
        throw new UnsupportedOperationException("this method not supported due to serialization reason");
        //operations.set(wrap(key), value, offset);
    }

    @Override
    public Long size(String key) {
        return operations.size(wrap(key));
    }

    @Override
    public Boolean setBit(String key, long offset, boolean value) {
        return operations.setBit(wrap(key), offset, value);
    }

    @Override
    public Boolean getBit(String key, long offset) {
        return operations.getBit(wrap(key), offset);
    }

    @Override
    public RedisOperations<String, V> getOperations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(wrap(key));
    }

    @Override
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(wrap(keys));
    }

    @Override
    public DataType type(String key) {
        return redisTemplate.type(wrap(key));
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(wrap(pattern));
    }

    @Override
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    @Override
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(wrap(oldKey), wrap(newKey));
    }

    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(wrap(oldKey), wrap(newKey));
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(wrap(key), timeout, unit);
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(wrap(key), date);
    }

    @Override
    public Boolean persist(String key) {
        return redisTemplate.persist(wrap(key));
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(wrap(key));
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(wrap(key), timeUnit);
    }

    @Override
    public void watch(String key) {
        redisTemplate.watch(wrap(key));
    }

    @Override
    public void watch(Collection<String> keys) {
        redisTemplate.watch(wrap(keys));
    }

    @Override
    public void unwatch() {
        redisTemplate.unwatch();
    }

    @Override
    public void multi() {
        redisTemplate.multi();
    }

    @Override
    public void discard() {
        redisTemplate.discard();
    }

    @Override
    public List<Object> exec() {
        return redisTemplate.exec();
    }

    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(wrap(key));
    }
}

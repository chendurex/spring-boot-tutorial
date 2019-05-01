package com.spring.boot.tutorial.redis.operation;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 针对key的操作
 * @see org.springframework.data.redis.core.RedisOperations
 * @author cheny.huang
 * @date 2019-01-05 17:28.
 */
public interface OptimizerRedisOperation {

    /**
     * @see RedisOperations#hasKey(Object)
     */
    @Nullable
    Boolean hasKey(String key);

    /**
     * @see RedisOperations#delete(Object)
     */
    @Nullable
    Boolean delete(String key);

    /**
     * @see RedisOperations#delete(Collection)
     */
    @Nullable
    Long delete(Collection<String> keys);

    /**
     * @see RedisOperations#type(Object)
     */
    @Nullable
    DataType type(String key);

    /**
     * @see RedisOperations#keys(Object)
     */
    @Nullable
    Set<String> keys(String pattern);

    /**
     * @see RedisOperations#randomKey()
     */
    @Nullable
    String randomKey();

    /**
     * @see RedisOperations#rename(Object, Object)
     */
    void rename(String oldKey, String newKey);

    /**
     * @see RedisOperations#renameIfAbsent(Object, Object)
     */
    @Nullable
    Boolean renameIfAbsent(String oldKey, String newKey);

    /**
     * @see RedisOperations#expire(Object, long, TimeUnit)
     */
    @Nullable
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * @see RedisOperations#expireAt(Object, Date)
     */
    @Nullable
    Boolean expireAt(String key, Date date);

    /**
     * @see RedisOperations#persist(Object)
     */
    @Nullable
    Boolean persist(String key);

    /**
     * @see RedisOperations#getExpire(Object)
     */
    @Nullable
    Long getExpire(String key);

    /**
     * @see RedisOperations#getExpire(Object, TimeUnit)
     */
    @Nullable
    Long getExpire(String key, TimeUnit timeUnit);

    /**
     * @see RedisOperations#watch(Object)
     */
    void watch(String key);

    /**
     * @see RedisOperations#watch(Object)
     */
    void watch(Collection<String> keys);

    /**
     * @see RedisOperations#unwatch()
     */
    void unwatch();

    /**
     * @see RedisOperations#multi()
     */
    void multi();

    /**
     * @see RedisOperations#discard()
     */
    void discard();

    /**
     * @see RedisOperations#exec()
     */
    List<Object> exec();

}

package com.spring.boot.tutorial.redis.scan;

import java.util.*;

/**
 * 实现单机scan功能的集群版本
 * @author cheny.huang
 * @date 2019-01-02 16:34.
 */
public interface ClusterScanOperation {
    /**
     * <p>集群版的scan</p>
     * <p>聚合所有redis服务端scan出来的key(数值不一定完全正确，有可能出现重复)</p>
     * @param pattern key(可以使用正则表达式)
     * @param limit 查询数量(不是返回数据阙值，而是在redis中查询的key的数量)
     * @see redis.clients.jedis.JedisCommands#sscan(String, String)
     * @return AggressionResult
     * {@link AggressionResult#getResult()}返回此次检索出来的key
     * {@link AggressionResult#hasNext()}如果为true则表示还有数据，那么下次检索请把对象带进去
     * @throws IllegalArgumentException if limit is exceed 10000 or pattern is null
     */
    AggressionResult scan(String pattern, int limit);

    /**
     * 再次索引剩余数据
     * @see #scan(String, int)
     */
    AggressionResult scan(String pattern, int limit, AggressionResult aggressionResult);
    class AggressionResult {
        /**
         * 存储redis返回数据的索引值，方便下次按照当前索引值继续扫描剩余的数据
         * 有个特殊场景是：如果redis返回0则表示已无数据，但是作为下次数据索引值时，则表示从第一个元素开始重新扫描
         * 所以这个索引值存在双重定义，既是下次索引值也是验证是否还存在元素的判定值
         */
        private Map<String, String> slotIndex = new HashMap<>();
        private List<String> result = new LinkedList<>();
        private static final String NOTHING_INDEX = "0";
        private static final String NOTHING_SLOT = "nothing";
        String nextSlot(String node) {
            if (node == null || node.isEmpty()) {
                return NOTHING_INDEX;
            }
            String slot = slotIndex.get(node);
            return slot != null ? slot : NOTHING_INDEX;
        }

        AggressionResult addSlot(String node, String index) {
            slotIndex.put(node, index);
            return this;
        }
        public List<String> getResult() {
            return result;
        }

        AggressionResult addResult(List<String> result) {
            this.result.addAll(result);
            return this;
        }

        /**
         * 如果slotIndex为空，则说明是构造的一个新的聚合对象，默认从第一个元素开始
         * 否则按照集群中存储的slot开始索引
         * @return
         */
        public boolean hasNext() {
            return slotIndex.isEmpty() || slotIndex.values().stream().filter(s->!NOTHING_INDEX.equals(s)).count() > 0;
        }

        boolean hasNext(String node) {
            String index = slotIndex.get(node);
            return index == null || !NOTHING_INDEX.equals(index);
        }

        AggressionResult nothing() {
            return new AggressionResult().addSlot(NOTHING_SLOT, NOTHING_INDEX);
        }
    }
}

package com.spring.boot.tutorial.transaction;

/**
 * @author chen
 *         2017/11/29 9:34
 */
public interface TransactionInvokerWithoutResult {
    /**
     * 在事务内执行，并且不需要返回值
     */
    void invoker();
}

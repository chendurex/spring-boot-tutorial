package com.spring.boot.tutorial.transaction;

/**
 * @author chen
 *         2017/11/29 9:32
 */
public interface TransactionInvokerWithinResult<T> {
    /**
     * 具体需要执行的事务动作
     * @return 业务结果
     */
    T invoker();
}

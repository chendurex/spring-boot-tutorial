package com.chen.spring.boot.advice.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

/**
 * @author chen
 *         2017/11/27 13:42
 */
public final class TransactionResourcesMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResourcesMonitor.class);
    public static void cleanResourceIfHas(){
        if (TransactionSynchronizationManager.getCurrentTransactionName() != null) {
            LOGGER.error("方法已经执行完毕了，居然还保存了事务，当前事务名称为：{}", TransactionSynchronizationManager.getCurrentTransactionName());
            return ;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            LOGGER.error("存在未提交的事务请求,当前请求资源内容为：{}", TransactionSynchronizationManager.getSynchronizations());
            TransactionSynchronizationManager.clear();
        }
        Map<Object, Object> resource = TransactionSynchronizationManager.getResourceMap();
        if (resource != null && !resource.isEmpty()) {
            for (Map.Entry entry : resource.entrySet()) {
                LOGGER.error("在事务范围内，未正常关闭资源，当前key：{}，value：{}", entry.getKey(), entry.getValue());
                TransactionSynchronizationManager.unbindResource(entry.getKey());
            }
        }
    }
}

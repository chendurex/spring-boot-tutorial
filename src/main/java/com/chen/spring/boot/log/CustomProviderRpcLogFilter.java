package com.chen.spring.boot.log;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.chen.spring.boot.log.print.ObjectMapperUtils;
import com.chen.spring.boot.log.print.TransactionResourcesMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * dubbo rpc 调用日志
 */
@Activate(group = {Constants.PROVIDER},order = -20000)
public class CustomProviderRpcLogFilter implements Filter {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = null;
        try {
            putMDCIfNeed(invocation);
            LOGGER.info("rpc call start\n" +
                    "invoker interface is :[{}]\n" +
                    "invoker methodName is :[{}]\n" +
                    "client ip is :[{}]\n" +
                    "invoker arguments is:{}",
                    invoker.getInterface(), invocation.getMethodName(),
                    RpcContext.getContext().getRemoteHost(), ObjectMapperUtils.get().writeValueAsString(invocation.getArguments()));

            long start = System.currentTimeMillis();
            result = invoker.invoke(invocation);
            TransactionResourcesMonitor.cleanResourceIfHas();
            if (result.hasException()) {
                LOGGER.error("rpc call error, error stack is : " , result.getException());
            } else {
                long elapsed = System.currentTimeMillis() - start;
                LOGGER.info("rpc call end and use time is :[{}]ms and resp value is: {}",
                        elapsed, ObjectMapperUtils.get().writeValueAsString(result.getValue()));
                if (elapsed > 3000) {
                    LOGGER.warn("slow api ,consume {} millis", elapsed);
                }
            }

        } catch (JsonProcessingException e) {
            LOGGER.warn("format value to jackson error, error stack is :", e);
        } finally {
            MDC.remove("uuid");
        }
        return result;
    }

    private void putMDCIfNeed(Invocation invocation) {
        final Object uuid = invocation.getAttachment("uuid") == null ? UUID.randomUUID().toString().replace("-", "") : invocation.getAttachment("uuid");
        MDC.put("uuid", uuid);
    }
}

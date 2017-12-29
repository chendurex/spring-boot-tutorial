package com.chen.spring.boot.log;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import org.apache.log4j.MDC;

import java.util.HashMap;
import java.util.Map;

/**
 * dubbo rpc 调用日志
 */
@Activate(group = {Constants.CONSUMER},order = -20000)
public class CustomConsumerRpcLogFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        putMDCIfNeed(invocation);
        return invoker.invoke(invocation);
    }

    private void putMDCIfNeed(Invocation invocation) {
        final Object uuid = MDC.get("uuid");
        if (uuid != null) {
            Map<String, String> attachments = invocation.getAttachments();
            if (attachments == null) {
                attachments = new HashMap<>();
            }
            attachments.put("uuid", uuid.toString());
        }
    }
}

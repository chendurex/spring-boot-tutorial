package com.spring.boot.tutorial.rabbitmq.interceptor;

import com.spring.boot.tutorial.rabbitmq.configuration.RabbitmqInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;

import java.util.UUID;

/**
 * 拦截写点日志
 * @author cheny.huang
 * @date 2018-07-30 17:28.
 */
public class RabbitListenerAroundAdvice implements RabbitmqInterceptor {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final String traceid = renderTraceIfAbsent();
        try {
            Message message = (Message) invocation.getArguments()[1];
            String body = new String(message.getBody());
            log.info("从消息队列：{}，接收到消息：{}", message.getMessageProperties().getConsumerQueue(),  body);
            return invocation.proceed();
        } finally {
            removeTraceidIfOwner(traceid);
        }

    }

    private static String renderTraceIfAbsent() {
        String traceIdKey = "traceid";
        String traceid = MDC.get(traceIdKey);
        if (traceid == null || traceid.isEmpty()) {
            traceid = UUID.randomUUID().toString();
            MDC.put(traceIdKey, traceid);
            // 如果是自己创建的traceid，那么在消费方消费完毕后也相应的移除traceid，防止对业务有入侵
            // 如果业务方已经存在traceid，那么就无需再次创建traceid
            return traceid;
        }
        return null;
    }

    private static void removeTraceidIfOwner(String origin) {
        String traceIdKey = "traceid";
        String traceid = MDC.get(traceIdKey);
        if (traceid != null && origin != null && traceid.equals(origin)) {
            MDC.remove(traceIdKey);
        }
    }
}

package com.spring.boot.tutorial.rabbitmq.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;

/**
 * 消息发送失败后，写日志
 * @author cheny.huang
 * @date 2018-07-30 17:28.
 */
public class ReturnCallBackListener implements ReturnCallback {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息发送失败, replyCode:{}, replyText:{}, exchange:{}, routingKey:{}", replyCode, replyText, exchange, routingKey);
    }
}
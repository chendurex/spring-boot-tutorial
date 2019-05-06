package com.spring.boot.tutorial.rabbitmq.configuration;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * 旧版本中，大家都是直接以string字符串发送消息，然后再人工转换为json对象，所以在传输消息头的内容为text/plain
 * 而Jackson2JsonMessageConverter在解析数据时会判断contentType是否包含json，如果不包含则不会解析数据，导致直接变为原始的
 * 字节数组传送到客户端
 * @author cheny.huang
 * @date 2019-01-04 14:13.
 */
public class CustomizedJackson2JsonMessageConverter extends Jackson2JsonMessageConverter {
    @Override
    public Object fromMessage(Message message, Object conversionHint) throws MessageConversionException {
        String contentType = message.getMessageProperties().getContentType();
        if (contentType == null || !contentType.contains("json")) {
            message.getMessageProperties().setContentType("application/json");
        }
        return super.fromMessage(message, conversionHint);
    }
}

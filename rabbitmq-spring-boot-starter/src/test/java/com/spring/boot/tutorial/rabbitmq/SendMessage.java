package com.spring.boot.tutorial.rabbitmq;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author cheny.huang
 * @date 2018-07-30 17:42.
 */
@Component
public class SendMessage extends BaseApplication {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testAll() throws Exception {
        sendString();
        sendMap();
        sendJavaObj();
        sendTopic();
        sendDirectExchange();
    }
    @Test
    public void sendString() {
        rabbitTemplate.convertAndSend(QueueConfiguration.TEST_STRING_QUEUE, "hello world");
    }

    @Test
    public void sendMap() {
        rabbitTemplate.convertAndSend(QueueConfiguration.TEST_MAP_QUEUE, ImmutableMap.of(1,1,2,2));
    }

    @Test
    public void sendJavaObj() {
        rabbitTemplate.convertAndSend(QueueConfiguration.TEST_JAVA_QUEUE, new TestA("hello", 11));
    }

    @Test
    public void sendTopic() {
        rabbitTemplate.convertAndSend(QueueConfiguration.TEST_TOPIC_EXCHANGE, QueueConfiguration.ROUTING_TOPIC_KEY_EXCHANGE, new TestA("hello", 11) );
    }

    @Test
    public void sendDirectExchange() {
        rabbitTemplate.convertAndSend(QueueConfiguration.TEST_DIRECT_EXCHANGE, QueueConfiguration.ROUTING_DIRECT_KEY_EXCHANGE, new TestA("hello", 11) );
    }

    public static class TestA implements Serializable {
        private String name;
        private Integer age;

        public TestA() {
        }
        TestA(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Integer getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

    }
}

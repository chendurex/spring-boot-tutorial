package com.spring.boot.tutorial.rabbitmq;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author cheny.huang
 * @date 2018-07-31 09:55.
 */
@Configuration
public class QueueConfiguration {
    final static String TEST_MAP_QUEUE = "spring.boot.test.map.queue";
    final static String TEST_STRING_QUEUE = "spring.boot.test.string.queue";
    final static String TEST_JAVA_QUEUE_KEY = "spring.component.mq.key";
    final static String TEST_JAVA_QUEUE = "spring.boot.test.java.queue";
    final static String TEST_TOPIC_EXCHANGE = "spring.boot.test.topic.exchange";
    final static String TEST_DIRECT_EXCHANGE = "spring.boot.test.direct.exchange";
    final static String TEST_TOPIC_EXCHANGE_QUEUE_A = "spring.boot.test.topic.exchange.queueA";
    final static String TEST_TOPIC_EXCHANGE_QUEUE_B = "spring.boot.test.topic.exchange.queueB";
    final static String TEST_TOPIC_EXCHANGE_QUEUE_C1 = "spring.boot.test.topic.exchange.queueC1";
    final static String TEST_TOPIC_EXCHANGE_QUEUE_C2 = "spring.boot.test.topic.exchange.queueC2";
    final static String TEST_TOPIC_EXCHANGE_QUEUE_D = "spring.boot.test.topic.exchange.queueD";
    final static String TEST_EXCHANGE_DIRECT_QUEUE_A = "spring.boot.test.direct.exchange.a";
    final static String TEST_EXCHANGE_DIRECT_QUEUE_B = "spring.boot.test.direct.exchange.b";
    final static String ROUTING_KEY_TOPIC_PREFIX_A = "spring.boot.test.topic.exchange";
    final static String ROUTING_KEY_TOPIC_PREFIX_B = "spring.boot.*.topic.exchange";
    final static String ROUTING_KEY_TOPIC_PREFIX_C1 = "spring.boot.test.#";
    final static String ROUTING_KEY_TOPIC_PREFIX_C2 = "#.test.topic.exchange";
    final static String ROUTING_KEY_TOPIC_PREFIX_D = "spring.*.test.#";
    final static String ROUTING_TOPIC_KEY_EXCHANGE = "spring.boot.test.topic.exchange";
    final static String ROUTING_DIRECT_KEY_EXCHANGE = "spring.boot.test.direct.exchange";

    @Bean
    Queue mapQueue() {
        return new Queue(TEST_MAP_QUEUE, true);
    }

    @Bean
    Queue stringQueue() {
        return new Queue(TEST_STRING_QUEUE, true);
    }

    @Bean
    Queue javaQueue() {
        return new Queue(TEST_JAVA_QUEUE, true);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(TEST_TOPIC_EXCHANGE);
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(TEST_DIRECT_EXCHANGE);
    }

   /* @Bean
    Queue exchangeQueueA() {
        return new Queue(TEST_TOPIC_EXCHANGE_QUEUE_A);
    }*/
    /**
     * 可以通过代码显式的声明绑定关系，也可以通过注解的方式声明
     */
    /*@Bean
    Binding binding() {
        return BindingBuilder.bind(exchangeQueueA()).to(topicExchange()).with(ROUTING_KEY_TOPIC_PREFIX_A);
    }*/

}

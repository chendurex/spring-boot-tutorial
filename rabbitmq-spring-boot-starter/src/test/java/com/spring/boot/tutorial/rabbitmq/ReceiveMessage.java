package com.spring.boot.tutorial.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author cheny.huang
 * @date 2018-07-30 17:49.
 */
@Slf4j
@Component
public class ReceiveMessage extends BaseApplication {

    @Test
    public void testReceiverMessage() throws Exception {
        TimeUnit.SECONDS.sleep(1000);
    }

    /**
     * 监听java对象消息，java对象可以自定义，对方传来的只要是符合标准的json数据，就可以以java对象接收
     * 如果监听的key不存在，或者被删除了，这种方式启动会失败
     * key支持SPEL表达式，比如可以将queues改为queues = "${spring.boot.java.queue}"，spring会自动解析值
     * 最好是将queue配置到配置中心中去，双方协议生成一个queue，
     * containerFactory的值不需要修改，这个由框架定义生成
     * demo {"name":"hello","age":11}
     * @param test
     */
    //@RabbitListener(queues = QueueConfiguration.TEST_JAVA_QUEUE)
    @RabbitListener(queues = "${"+QueueConfiguration.TEST_JAVA_QUEUE_KEY+"}")
    public void receiveJavaObj(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java object message :" + test);
    }

    /**
     * 监听map消息
     * 如果监听的key不存在或者被删除了，这种方式启动会失败
     * demo {"1":1,"2":2,"3":3}
     * @param map
     */
    @RabbitListener(queues = QueueConfiguration.TEST_MAP_QUEUE)
    public void receiveMapObj(Map<Integer, Integer> map) {
        assertFalse(map.isEmpty());
        log.info("start receive java map message :" + map);
    }

    /**
     * 可以直接在控制台界面填充 1111 或者 "{\"result\":200,\"uid\":131723,\"appName\":\"oa-app\",\"ip\":\"192.168.3.33\",\"interfaceName\":\"userData.sync\",\"serviceName\":\"/biz/t8t-sys-imd/dataserver\"}\n"
     */
    @RabbitListener(queuesToDeclare = {@Queue(name = QueueConfiguration.TEST_STRING_QUEUE, durable = "true")})
    public void receiveStringV(String v) {
        assertNotNull(v);
        log.info("start receive string message :" + v);
        throw new RuntimeException();
    }

    /**
     * <p>监听广播消息，某个消息是否为广播，这个由生产者来决定的，消费者只要按照生产者的标准配置，则自动会收到广播消息
     * 比如生产者的exchange为：spring.boot.test.topic.exchange，定义的路由key为：spring.boot.test.topic.exchange
     * 那么消费者可以有多种选择消费数据</p>
     * 1. spring.boot.test.topic.exchange 消费者的路由key就是跟生成者一模一样，则表示固定消费当前消息，这个与exchange direct模式一样{@link #receiveDirectExchangeA(TestB)}
     * 2. spring.boot.*.topic.exchange 消费者通过占位符*指定路由策略，注意一个*只能代表一个单词，如果需要多个单词组合，则需要多个*
     * 3. spring.boot.test.#或者#.test.topic.exchange #表示多个符号，但是只能放在首位或者末位
     * 4. spring.*.test.# #号和*号组合方式
     * @param test
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_TOPIC_EXCHANGE_QUEUE_A, durable = "true"),
            exchange = @Exchange(value = QueueConfiguration.TEST_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QueueConfiguration.ROUTING_KEY_TOPIC_PREFIX_A))
    public void receiveTopicExchangeA(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java topic A message :" + test);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_TOPIC_EXCHANGE_QUEUE_B, durable = "true"),
            exchange = @Exchange(value = QueueConfiguration.TEST_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QueueConfiguration.ROUTING_KEY_TOPIC_PREFIX_B))
    public void receiveTopicExchangeB(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java topic B message :" + test);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_TOPIC_EXCHANGE_QUEUE_C1, durable = "true"),
            exchange = @Exchange(value = QueueConfiguration.TEST_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QueueConfiguration.ROUTING_KEY_TOPIC_PREFIX_C1))
    public void receiveTopicExchangeC1(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java topic C1 message :" + test);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_TOPIC_EXCHANGE_QUEUE_C2, durable = "true"),
            exchange = @Exchange(value = QueueConfiguration.TEST_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QueueConfiguration.ROUTING_KEY_TOPIC_PREFIX_C2))
    public void receiveTopicExchangeC2(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java topic C2 message :" + test);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_TOPIC_EXCHANGE_QUEUE_D, durable = "true"),
            exchange = @Exchange(value = QueueConfiguration.TEST_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = QueueConfiguration.ROUTING_KEY_TOPIC_PREFIX_D))
    public void receiveTopicExchangeD(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java topic D message :" + test);
    }
    /**
     * 固定exchange接收消息，其实属于广播中的一种，只是路由规则固定了
     * 下面这些注解也可以通过代码方法配置
     * @see #receiveTopicExchangeA(TestB)
     * @param test
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_EXCHANGE_DIRECT_QUEUE_A, durable = "true"),
            exchange = @Exchange(QueueConfiguration.TEST_DIRECT_EXCHANGE),
            key = QueueConfiguration.ROUTING_DIRECT_KEY_EXCHANGE))
    public void receiveDirectExchangeA(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java direct exchange message A :" + test);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = QueueConfiguration.TEST_EXCHANGE_DIRECT_QUEUE_B, durable = "true"),
            exchange = @Exchange(QueueConfiguration.TEST_DIRECT_EXCHANGE),
            key = QueueConfiguration.ROUTING_DIRECT_KEY_EXCHANGE))
    public void receiveDirectExchangeB(TestB test) {
        assertFalse(test.getName() == null);
        log.info("start receive java direct exchange message B :" + test);
    }

    public static class TestB implements Serializable {
        private String name;
        private Integer age;

        public TestB() {

        }
        public TestB(String name, Integer age) {
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

        String getName() {
            return name;
        }
        @Override
        public String toString() {
            return "name is : " + name + ", and age is :" + age;
        }

    }
}

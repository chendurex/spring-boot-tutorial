#### 引入背景
  + 原始的`MQ`，在使用方式上非常槽糕，需要自己实现连接的创建以及消息的发送等，严重增加`MQ`的维护成本
  + 有大量的同事为了使用`MQ`，而使用了公司自研的`EDA`框架，这个框架最开始的初衷仅仅是为了实现分布式事务，而不是作为一个MQ中间件而准备，
     而且EDA框架也并非真正的解决了分布式事务的问题，仅仅只是增加了一点点的可靠性，但是增加了维护成本以及学习成本(有计划移除`EDA`这个中间件)
  + 在调用链路非常长的情况，有时候仅仅是为了通知下游，那这种场景就需要`MQ`了，让`MQ`来降低服务之间的依赖关系
  + 服务与服务之间存在非常多的相互依赖关系，而且如果某个上下游出现问题，整个调用链路都将失败，但是有时候又无法保证在某个时刻是否会失败，
     此时，需要一个MQ来保证交互的正确性，只要上下把数据发送到`MQ`，就代表成功了，至于下游是否真正的接收到消息，这个是下游的事情，上游已经解放了
  
#### 引入方式
  + 项目已经是`spring boot`项目，支持自动配置等功能
  + 添加依赖
  
                  <dependency>
                    <groupId>com.spring.tutorial</groupId>
                    <artifactId>rabbitmq-spring-boot-starter</artifactId>
                  </dependency>  

   + 遵守我们的基本依赖约定，引入依赖则代表自动开启服务，不需要时候则直接删除依赖
          
#### 使用方式
  + 消息生成者
    + 发送消息时，首先要求存在`queue`，如果不存在则需要先声明，否则会出现消息发送出去了，但实际未发现有任何消息存在队里中
      如下是使用`spring`的方式声明`queue`
      
              @Configuration
              public class QueueConfiguration {
                  final static String TEST_MAP_QUEUE = "spring.boot.test.map.queue";
              
                  @Bean
                  Queue mapQueue() {
                      return new Queue(TEST_MAP_QUEUE, true);
                  }
              }
                
    + 使用`spring`集成的`rabbitTemplate`发送普通的`queue`消息，如：
        
            @Autowired
            private RabbitTemplate rabbitTemplate;
            public void sendQueue() {
                rabbitTemplate.convertAndSend(TEST_MAP_QUEUE, new TestA("hello", 11));
            }
            
    + 使用`spring`集成的`rabbitTemplate`发送`topic`消息与发送`queue`消息类似，仅仅是多个一个`key`，如：
    
            @Autowired
            private RabbitTemplate rabbitTemplate;
            public void sendTopic() {
                rabbitTemplate.convertAndSend("spring.boot.test.topic.exchange", "spring.boot.test.exchange", new TestA("hello", 11) );
            }
            
        + `spring.boot.test.topic.exchange`这个值是`exchange key`可以在控制台上`exchanges`查看到对应的`key`，表示这个`key`为`topic` 
        + `spring.boot.test.exchange`这个值是路由key，如果有消费者需要监听当前广播地址，那么可以根据当前路由key来指定路由规则，
           比如消费者指定的路由策略为：`spring.boot.test.#`，表示`spring.boot.test.topic.a.queue`和`spring.boot.test.topic.b.queue`都会收到消息
        + 注意：同一个exchange可以发送多个路由key，消费者可以根据不同的路由key按需消费(一个exchange代表一个连接，所以能复用尽量复用)   
        + 如果有消费者需要监听广播消息，你需要将这两个值都给到消费者
        
  + 消费者
      +  队列消费，`containerFactory`无需修改，仅需需要`queue`中的`name`，改为监听的队列名称(可以使用`SPEL`表达式)， 
          `@RabbitListener`这个注解里面是可以定义`queue`的，我这里采用`queuesToDeclare`的方式定义会比`queue`多个功能点，
          `rabbitmq`在监听消息时，必须保证所监听的队列存在，如果不存在则提示错误，采用`queuesToDeclare`方式则表示如果监听的`key`
          不存在则自动声明一个，而原始的`queue`方式是没有这个功能
        
        
                      // 也可以使用占位符的方式从配置中心获取队列名
                      // @RabbitListener(queuesToDeclare = {@Queue(name = "${spring.boot.test.java.queue}", durable = "true")})
                      @RabbitListener(queuesToDeclare = {@Queue(name = "spring.boot.test.java.queue", durable = "true")})
                      public void receiveJavaObj(TestB test) {
                          Assert.assertFalse(test.getName() == null);
                          System.out.println("start receive java object message :" + test);
                      }
              
              
      + 广播消费，消费的方式与队列一样，但是需要而外配置一个绑定关系，绑定某个广播与队列之间的关系，最终表现出来的形式就是，
          `rabbitmq`发送消息有广播和队列两种方式，消费消息只有队列。(这个跟`rabbitmq`规范有关系，`rabbitmq`也是实现了`jms`规范，
          注意是实现`jms`规范，内部通信协议是`amqp`，这里要注意。而`jms`规范要求交互方式就是`p2p`，所以`rabbitmq`采用了虚拟路由的方式
          实现了广播，包括`activemq`也是采用了类似的方式)
    
    
                           @RabbitListener(bindings = @QueueBinding(
                                   value = @Queue(value = "self.definition.queue.key", durable = "true"),
                                   exchange = @Exchange(value = "producer.topic.exchange.key", type = ExchangeTypes.TOPIC),
                                   key = "self.definition.route.key.#"))
                           public void receiveTopicExchangeA(TestB test) {
                               assertFalse(test.getName() == null);
                               log.info("start receive java topic A message :" + test);
                           }
   
   
          + `producer.topic.exchange.key`: 需要监听的`topic key`,这个是需要找广播发送者获取的`key`
          + `self.definition.queue.key`: 这个是自己本地服务监听的`key`，注意这个`key`不要与其它的`queue name`相同
          + `self.definition.route.key.#`: 路由策略，也就是说匹配到了某些策略的消息才会被真正发送到`self.definition.queue.key`队列中
              这个是可以由消费者指定的路由策略。如生产者会给你一个`spring.boot.test.exchange`这个`exchange key`和`spring.boot.route.exchange`
              这个route key(当然exchange key和route key可以保持一致)，那么你定义的exchange为：`spring.boot.route.exchange`，
              route key为：`spring.boot.route.#`表示接收以spring.boot.route.开头的队列
          
          + 路由策略种类(route key)：
              1. `spring.boot.test.topic.exchange` 消费者的路由key就是跟生成者一模一样，则表示固定消费当前消息，这个与`exchange direct`模式一样{@link #receiveDirectExchangeA(TestB)}
              2. `spring.boot.*.topic.exchange` 消费者通过占位符*指定路由策略，注意一个*只能代表一个单词，如果需要多个单词组合，则需要多个*
              3. `spring.boot.test.#`或者`#.test.topic.exchange` #表示多个符号，但是只能放在首位或者末位
              4. `spring.*.test.#` #号和*号组合方式
              
      + 消费者默认是不做确认的(不管消费成功，都算成功)，而且是不带事务的，如果需要开启自动确认，请在配置中心增加`spring.rabbitmq.mode=auto`，这种消费模式表示
       只要业务不抛出`RuntimeException`代表消费成功，否则消费失败。消费失败后，会继续补发消息到消费者，重复发送10(`spring.rabbitmq.maxAttempts`)次后还是消费失败，则不会继续补发

#### 注意事项
  + 如果按照文档方式接入消息队列还是有问题，则可以查看测试代码，里面包括了如何定义MQ,发送消息，接收消息，大家可以按照模板进行接入
  + 消息是否发送成功，可以通过控制台界面查看([测试环境控制台](http://test.rabbitmq.com:15672/)/[开发环境控制台](http://192.168.2.56:15672/))
    + 如果队列不存在，则说明未声明队列，请查看文档如何声明队列
    + 如果队列存在，但是队列未存在消息，请检查队列名称是否正确，或者是否连接到正确的mq服务器
  + 消息是否消费成功，可以通过控制台界面查看
    + 如果队列不存在，或者消息为空，则要求消息生产者先发送消息
    + 或者请检查本消息是队列还是广播模式，如果是队列模式，是否存在多个消费者
    + 如果是广播模式，请检查是否按照要求配置正确
  + 如果发现指定了广播消息队列`key`，但是收不到广播，请到控制台把消息key删除，让程序重新生成一份(因为最开始队列key已经保存了信息，所以需要删除原始数据重新生成信息)
  + 消息监中定义的方法返回类型一定要是`void`类型，如果不是`void`则表示需要将消息回复给对方，但是我们目前并未支持此方式，从而导致消息重复消费多次  
  + 如果消费者连接一个未定义的队列，那么会报出如下错误。解决方案有三种
      1. 让生产者在`broker`上面生成这个`queue`
      2. 通过控制台定义这个`queue`
      3. `RabbitListener`增加`queuesToDeclare`参数，如上面`demo`所示
      
  
              Caused by: org.springframework.amqp.rabbit.listener.QueuesNotAvailableException: Cannot prepare queue for listener. Either the queue doesn't exist or the broker will not allow us to use it.
                at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.start(BlockingQueueConsumer.java:620)
                at org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer$AsyncMessageProcessingConsumer.run(SimpleMessageListenerContainer.java:996)
                at java.lang.Thread.run(Thread.java:745)
              Caused by: org.springframework.amqp.rabbit.listener.BlockingQueueConsumer$DeclarationException: Failed to declare queue(s):[spring.boot.test.x]
                at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.attemptPassiveDeclarations(BlockingQueueConsumer.java:711)
                at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.start(BlockingQueueConsumer.java:588)
                ... 2 common frames omitted
              Caused by: java.io.IOException: null
                at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:126)
                at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:122)
                at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:144)
                at com.rabbitmq.client.impl.ChannelN.queueDeclarePassive(ChannelN.java:991)
                at com.rabbitmq.client.impl.ChannelN.queueDeclarePassive(ChannelN.java:52)
                at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
                at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                at java.lang.reflect.Method.invoke(Method.java:483)
                at org.springframework.amqp.rabbit.connection.CachingConnectionFactory$CachedChannelInvocationHandler.invoke(CachingConnectionFactory.java:991)
                at com.sun.proxy.$Proxy122.queueDeclarePassive(Unknown Source)
                at org.springframework.amqp.rabbit.listener.BlockingQueueConsumer.attemptPassiveDeclarations(BlockingQueueConsumer.java:690)
                ... 3 common frames omitted
              Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue 'spring.boot.test.x' in vhost '/', class-id=50, method-id=10)
                at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
                at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:36)
                at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:494)
                at com.rabbitmq.client.impl.AMQChannel.privateRpc(AMQChannel.java:288)
                at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:138)
                ... 12 common frames omitted
  
  + 如果提示如下内容，则是因为序列化失败导致的，框架会将接收参数自动转换为jackson对象，请检查接收数据是否对象
            
            
            Caused by: org.springframework.amqp.support.converter.MessageConversionException: Failed to convert Message content
                at org.springframework.amqp.support.converter.Jackson2JsonMessageConverter.fromMessage(Jackson2JsonMessageConverter.java:197)
                at com.to8to.component.mq.configuration.CustomizedJackson2JsonMessageConverter.fromMessage(CustomizedJackson2JsonMessageConverter.java:21)
                at org.springframework.amqp.support.converter.Jackson2JsonMessageConverter.fromMessage(Jackson2JsonMessageConverter.java:159)
                at org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener.extractMessage(AbstractAdaptableMessageListener.java:262)
                at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter$MessagingMessageConverterAdapter.extractPayload(MessagingMessageListenerAdapter.java:266)
                at org.springframework.amqp.support.converter.MessagingMessageConverter.fromMessage(MessagingMessageConverter.java:118)
                at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.toMessagingMessage(MessagingMessageListenerAdapter.java:168)
                at org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter.onMessage(MessagingMessageListenerAdapter.java:115)
                at org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer.doInvokeListener(AbstractMessageListenerContainer.java:1414)
                ... 25 common frames omitted
            Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `com.rabbitmq.client.Delivery` (no Creators, like default construct, exist): cannot deserialize from Object value (no delegate- or property-based Creator)

#### 引用
  + [spring boot](https://spring.io/guides/gs/messaging-rabbitmq/)
  + [rabbit mq](https://www.rabbitmq.com/)
  + [阿里云如何定义MQ最基本使用场景](https://help.aliyun.com/document_detail/29532.html?spm=5176.7946988.846996.gdg.3eb33a5dCqVR6L)
  + [rabbitmq-for-beginners-exchanges-routing-keys-bindings](https://www.cloudamqp.com/blog/2015-09-03-part4-rabbitmq-for-beginners-exchanges-routing-keys-bindings.html)
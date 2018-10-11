### 一、简介
  + 说明：这是一个`springboot`框架，完全采用`springboot`进行开发的框架(version:`2.0.0.RELEASE`)
  + Spring Boot提供了一个快速（和自治的）的方式来创建一个基于Spring的生产环境。它是基于Spring框架，支持约定优于配置，并且被设计成尽可能快地让你启动和运行程序。[传送门](https://docs.spring.io/spring-boot/docs/2.0.0.RELEASE/reference/htmlsingle/)
  
  + 这个项目仅仅是个人在工作中使用到或者业余项目使用到的，然后拼拼凑凑加上去的，为了以后遇见同等问题有记录，完全是为了个人所用
  
#### 目前遇见的问题以及引入`springboot`理由
  + 第一次部署项目的时候，需要将项目与`tomcat`集成，或者在修改配置、增加依赖的时候，也有可能需要再次配置`tomcat`，这是一个重复而且繁琐的过程，希望能避免
  
  + 我们希望达成一种共识，把所有的与配置有关的东西都统一放到一个地方，每次有新的组件引入，仅仅需要加入对应的`jar`即可(并且组件是可以做到统一维护，这样开发者根本不用关心具体配置问题
     每次添加一个新的依赖，我们仅需要去`build.gradle`文件中添加对应的配置，而不用而外添加配置，而且有利于开发者快速了解公司的整体架构
     
  + 每次底层提供新的组件或者修改配置(如添加`filter`、`listener`、`servlet`、`interceptor`等)都需要每个项目进行统一修改，非常不利于架构上的统一管理

  + 我们的配置包括但不限于组件配置、`tomcat`配置、属性资源配置、上下文配置等、散落在各个不同的地方，虽然我们有配置中心，但是还是没有做到所有的配置都统一管理，因为目前的框架是无法
     做到所以的配置都放入到配置中心，所以增加了架构的复杂度，也增加了学习架构的成本，我们应该将所有的配置都放入到配置中心统一管理
     
  + 目前项目是基于`spring`开发的，而且`90%`以上的配置都是通过扫描包的方式注入到容器中的，假如存在某个`A bean`依赖`B bean`的情况，那么我们希望`B bean`先注入到容器，再注入`A bean`，
     正常情况使用`@autowire`注解确实是没有问题，但是有时候我们的`bean`可能不需要被容器所管理(如：统一生成`UUID`的`UUIDUtil`工具类，它是通过`SpringContextHolder.getBean("stringRedisTemplate2")`
     的方式获取`bean`)假如在`bean A`实例化时`bean B`没有被容器注入，那将导致`bean A`也实例化失败，此时我们只能依靠人为的规则去要求`bean B`需要提前实例化(此时架构师需要了解每个配置初始化
     顺序，假如配置顺序要求越来越多的时候，那将是个非常大的缺陷，或者玩意架构师也忘记了初始化顺序的时候，那就有灾难了)
     
  + 每个组件之间也会存在依赖关系(如：`MQ`组件必须得依赖基础的架构启动完毕，`MQ`才能启动，否则会出现`MQ`刚刚启动，但是基础架构未启动完成，`MQ`就已经开启线程消费消息，但是基础架构未启动完毕，导致消息消费失败，
     当然，还有`duubo`也有类似的情况，因为`dubbo`在项目启动的时候会另外开启线程去提供服务，而此时又有新的请求刚刚被分配到此机器)
     此时，我们需要一个控制各个组件启动顺序的功能
     
  + 我们在使用`RPC`的过程中，目前能做的只是监控了各种业务之间入参、出参以及耗时等基本功能，还未做到将一个功能的整个调用链串联起来，即未实现调用链网(当然，我们可以生成一个唯一的`MDC`，
     然后在框架层统一利用日志控制，但是仅仅做到了调用链统一而已，与我们期望的监控以及顺序以及界面渲染方面，还是存在非常大的差距)，此时，我们应该引入一个这样的框架，帮我们实现监控等场景

#### 基本约定
  + 版本依赖：按照`springboot`提供的版本为标准，这样方便后期维护以及减少出现兼容性问题(如果出现已经出现了兼容性问题，如无必要，请按照`springboot`版本为标准)   
  + 基本配置：我们应该遵循`约定优于配置`的方式，每个配置文件应该放入到默认的位置，这样方便维护以及扩展，如无必要，不要以编程的方式修改默认配置项
  + 系统配置：任何的系统配置项(即在`properties`属性项，包括但不限于JDBC、tomcat、context-param等)都在配置中心配置(在配置中心搜索`springboot`查看所有的配置)，请不要直接配置到`application`文件中，
       下面对功能扩展中，使用的是以配置文件的方式进行说明，但是真正使用时，请将配置放置到配置中心
          
#### 如何部署项目
  + 项目构建是基于`gradle 4.+`版本，所以构建之前，请确定你的版本是否正确
  + 请确认项目配置文件`application.properties`位于根目录下
  + `gradle.build`按如下方式修改(除了修改`pro_name`和`child_version`其它的保持一致)：
    
            ext{
                pro_type    = 'boot'// springboot项目则改为boot
                child_version = '1.0.0'//项目版本号
            }
            apply plugin: 'java'
            apply plugin: "com.chen.rootconfig"

            buildscript {
                repositories {
                    maven { url "http://nexus.chen.com/nexus/content/groups/public/" }
                }
                dependencies {
                    classpath 'com.chen:chen-gradle-plugin:+'
                    classpath "org.springframework.boot:spring-boot-gradle-plugin:1.5.6.RELEASE"
                }
            
                configurations.all {
                    resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
                }
            }
    
  + 通过命令行构建项目是`war`包，然后按照`war`项目部署


#### 如何使用测试

        @RunWith(SpringRunner.class)
        @SpringBootTest(classes = SpringBootStart.class)
        public class SpringBootRunnerTest {
            @Autowired
            private IPerfAccessService accessService;
        
            @Test
            public void run() {
                Assert.assertTrue(accessService.checkAccess(1111L));
            }
        }    
    
#### [如何开发一个基于boot的组件](http://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/htmlsingle/#boot-features-custom-starter)

#### 如何扩展配置
  + 实现方式都是通过添加`Configuration`注解以代表一个配置，但是启动时有两种方式
    + 第一种：`Configuration`是基于`Component`注解，所以只要配置的扫描路径能扫描到，即可实现注入配置
    + 第二种：通过`spring boot`约定的方式，将配置加入到`spring.factories`文件中(具体可以查看上文提到的：如何开发一个基于boot的组件)，
              官网推荐这种方式，因为`Configuration`就是代表一个配置，既然是配置，那么应该遵循配置的约定(类似XML文件)，这样使用者会非常清楚当前架构的设计，
              而且采用这种方式可以控制配置生成的顺序，而采用注解是无法实现的
  + 如果需要控制顺序，那么请在`Configuration`配置文件中使用`AutoConfigureOrder`、`AutoConfigureAfter`、`AutoConfigureBefore`等属性
  + 如果将配置放入配置中心，则一定要加上`模块`，不然会导致所有的项目生效，出现大面积系统故障
  + 如果需要控制生成的条件，那么请使用`ConditionalOn*`等注解，[实现机制可以看这里](https://dzone.com/articles/how-springboot-autoconfiguration-magic-works)
  + 扩展`filter`:
     + 添加`filter`:
     
            @Configuration
            public class CustomFilter {
                @Bean
                @ConditionalOnClass(xxxFilter.class)
                public FilterRegistrationBean assertionThreadLocalFilter() {
                    FilterRegistrationBean bean =  new FilterRegistrationBean();
                    bean.setFilter(new xxxFilter());
                    bean.setName(assertionThreadLocalFilter.getSimpleName());
                    bean.addUrlPatterns("/*");
                    return bean;
                }
             }   
     + 删除`filter`: 在`application.properties`文件中添加要禁用的`filter`过滤器全限定名，多个以逗号分割
        spring.chen.filter=com.chen.web.module.base.adapter.ExtendInitSessionFilter,org.jasig.cas.client.session.SingleSignOutFilter
     + `filter`顺序：如果对`filter`有顺序要求，那么请设置`order`属性,值越小优先级越高，如果需要在某个系统自带的`filter`之前或者之后，
                     自行查看`com.chen.spring.boot.CustomFilter`类中定义的所有`filter`顺序
  + 扩展`servlet`：
     + 添加`servlet`:
         
             @Configuration
             public class CustomServlet {
             
                 @Bean
                 public ServletRegistrationBean xxxServlet() {
                     ServletRegistrationBean bean = new ServletRegistrationBean();
                     bean.setServlet(new xxxServlet());
                     bean.setName("xxxServletName");
                     bean.setLoadOnStartup(2);
                     bean.setUrlMappings("/*");
                     return bean;
                 }
             }
     
     + 删除`servlet`: 在`application.properties`文件中添加要禁用的`servlet`全限定名，多个以逗号分割
               spring.chen.servlet=com.chen.web.platform.servlet.ApplicationServlet
                   
  + 扩展`listener`：
     + 添加`listener`


               @Configuration
               public class CustomListener {

                   @Bean
                   public ServletListenerRegistrationBean<IntrospectorCleanupListener> introspectorCleanupListener() {
                       ServletListenerRegistrationBean<IntrospectorCleanupListener> bean = new ServletListenerRegistrationBean<>();
                       bean.setListener(new IntrospectorCleanupListener());
                       return bean;
                   }
               }
       
       
  + 扩展`interceptor`：按照`Configuration`配置，并且继承`WebMvcConfigurerAdapter`，如果对象需要被`spring`容器监管则定义成`bean`，否则可以不需要
  

          @Configuration
          public class InterceptorConfig extends WebMvcConfigurerAdapter {
              @Bean
              @Autowired
              public HandlerInterceptor customSysLogInterceptor() {
                  return new CustomSysLogInterceptor();
              }

              @Override
              public void addInterceptors(InterceptorRegistry registry) {
                 // 默认拦截所有请求，如果需要拦截个别请求，可以追加匹配规则，多个以逗号隔开
                 // registry.addInterceptor(customSysLogInterceptor()).addPathPatterns("/**/getTableColumn.do", "/**/getAll.do");
                  registry.addInterceptor(customSysLogInterceptor());
              }

          }
      
  + 扩展系统上下文(`<context-param>`)：在`application.properties`文件中添加如下配置：
  
         spring.chen.context-param.customConfigKey1=customConfigValue1
         spring.chen.context-param.customConfigKey2=customConfigValue2


### spring cloud简介
  + `Spring Cloud`为开发人员提供了快速构建分布式系统中一些常见模式的工具（例如配置管理，服务发现，断路器，智能路由，微代理，控制总线）。
    分布式系统的协调导致了样板模式, 使用`Spring Cloud`开发人员可以快速地支持实现这些模式的服务和应用程序。他们将在任何分布式环境中运行良好，
    包括开发人员自己的笔记本电脑，裸机数据中心，以及`Cloud Foundry`等托管平台
  + 以下为`Spring Cloud`的核心特性：
    + 服务注册和发现(`eureka`)
    + 服务与服务调用(`feign`)
    + 路由和服务网关(`zuul`/`spring cloud api gateway`)
    + 负载均衡(`ribbon`)
    + 断路器(`hystrix`)
    + 分布式链路跟踪器(`sleuth`/`zipkin`)
  + 为什么考虑Spring Cloud
    + Spring Cloud来源于Spring，质量、稳定性、持续性都可以得到保证
    + Spirng Cloud天然支持Spring Boot，更加便于业务落地
    + Spring Cloud发展非常的快，从16年开始的时候相关组件版本为1.x，到现在将要发布2.x系列
    + Spring Cloud是Java领域最适合做微服务的框架
    + 相比于其它框架,Spring Cloud对微服务周边环境的支持力度最大
    + 对于中小企业来讲，使用门槛较低

#### `spring cloud` 组件之`Eureka`简介
  + [传送门](http://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/1.3.5.RELEASE/single/spring-cloud-netflix.html#_service_discovery_eureka_clients)
  + `Eureka`是`Netflix`开源的一款提供服务注册和发现功能的产品，也是`springcloud`体系中最重要最核心的组件之一
       管理各种服务功能包括服务的注册、发现、熔断、负载、降级等，比如dubbo admin后台的各种功能
  + ![](http://note.youdao.com/yws/api/personal/file/363A48A0894F43C09ACC79C0BD27835D?method=download&shareKey=ad2658e2fdf3a7fcf960becd48d887ab)
  + Eureka Server：提供服务注册和发现
  + Service Provider：服务提供方，将自身服务注册到Eureka，从而使服务消费方能够找到
  + Service Consumer：服务消费方，从Eureka获取注册服务列表，从而能够消费服务
  + 客户端与服务端交互的默认地址：`http://localhost:8761/eureka`，多个地址可使用 , 分隔(注意：本公司的端口号注册地址为：http:eureka.daydao.com/eureka)
  + [eureka详情](https://github.com/chendurex/chen-spring-boot-starter/wiki/EUREKA)  
#### Hystrix
  + 声明：文档中的描述是以`spring cloud`集成`hystrix`而叙述，有些功能是`hystrix`本身有的，但是由于`spring cloud`集成原因而导致局限
  + 如何使用或者避免`fallback`
    + 如果系统抛出非预期的异常，那么就会`fallback`，[(点击查看`Hystrix`定义的异常列表)](https://github.com/Netflix/Hystrix/wiki/How-To-Use#execution-exception-types)
    + 除了`HystrixBadRequestException`异常，所有的其它异常都会被`metrics`监控，然后触发`fallback`策略。所以，一般的参数错误等需要将错误包装成
     `HystrixBadRequestException`，或者自定义`no fallback exception`，然后再通过`getCause`获取信息，这样就不会被统计到`metrics`错误信息中，也就不会触发`fallback`
    + 因为`hystrix`将超时作为失败处理，所以导致`fallback`，所以在设计的时候一定要控制好超时时间
    + 一般的`RPC`在业务调用的异常或者超时时，会自动选择重试，而`hystrix`则直接作为`fallback`处理，所以在负载这块，也是需要重点考虑，防止频繁的`fallback`，而返回非预期数据。
      或者在返回`fallback`时，带上错误码，然后通过框架层再次选择重试其它机器。否则需要考虑将超时作为一个正向流程，让框架自动选择重试，这样就需要改造下`hystrix`。当然负载的时候
      设置的`负载超时时间*重试次数<服务端超时时间`，客户端会继续选择重试其它服务端，但是这样就需要高效的实现幂等功能
    + `Hystrix fallback`策略：先获取缓存，如果缓存没有再提供默认(请注意：我指的是`fallback`策略，正常情况下，
      需要远程调用数据则远程调用，除非缓存数据对业务影响不大，而且在一定的时效能更新缓存，也可以先从缓存获取数据)
    + 客户端配合`feign`时，局限非常严重，仅仅支持`fallback`，我们应该将控制放到服务端
    + 业务方仅需在`fallback`方法中，添加一个`Throwable`参数，就可以获取错误信息
  + 如果是配置了`hystrix`和`ribbon`，那么一定要注意`hystrix`的超时时间大于`ribbon`最大重试次数的超时时间，防止提前进入超时
  + [服务端属性配置传送门](https://github.com/Netflix/Hystrix/wiki/Configuration)(注意：`spring`与`hystrix`的属性`key`可能不一样，
     `spring key`可以通过`HystrixPropertiesManager`类查看，`netflix key` 通过`HystrixCommandProperties`查看，这里我以`spring key`的形式进行说明)
    + `fallback.enabled`：是否开启`fallback`，默认`true` 
    + `execution`配置参数 
      + `execution.isolation.strategy`：执行策略，默认是`THREAD` 
      + `execution.isolation.thread.timeoutInMilliseconds`：设置线程执行超时时间，默认`1`秒
      + `execution.timeout.enabled`：是否开启超时，默认`true`  
      + `execution.isolation.thread.interruptOnTimeout`：到了超时时间后，是否调用`interrupt`方法，默认`true`
      + `execution.isolation.semaphore.maxConcurrentRequests`：在开启`semaphore`模式下最大并发调用，默认10
    + `semaphore`模式：由于`semaphore`模式用的比较少，而且目前发现流控方面有一点问题，暂时不深入研究
    + 熔断器配置参数：因为`spring cloud`配合`feign`在客户端方面的支持局限太过严重(仅仅支持`fallback`)，导致熔断功能大打折扣，几乎无用武之地(`todo`下列参数还未测试)
      + `circuitBreaker.enabled`：是否开启熔断器功能，默认`true`
      + `circuitBreaker.requestVolumeThreshold`：在某个时间段(`metrics.rollingStats.timeInMilliseconds`)失败量达到这个值后开启熔断器，默认`20`个
      + `circuitBreaker.sleepWindowInMilliseconds`：熔断器开启多少时间后，再次接收请求，默认`5`s
      + `circuitBreaker.errorThresholdPercentage`：错误率在某个时间段(`metrics.rollingStats.timeInMilliseconds`)达到多少百分比后，开启熔断器，默认`50`
      + `circuitBreaker.forceOpen`：是否强制开启熔断器，拒绝所有请求，默认`false`
      + `circuitBreaker.forceClosed`：是否强制关闭熔断器，不管是否失败，接收所有请求，默认`false`
    + 数据采集配置参数：  (`todo`下列参数还未测试)
      + `metrics.rollingStats.timeInMilliseconds`：熔断器监控时间段，在这个时间段内出现了熔断阙值，则开启熔断，默认`10s`
      + `metrics.rollingStats.numBuckets`：熔断器监控时间段桶的数量，也就是说，某个时间段被分为多少个桶，每个桶占用多少数据量。比如10s，10个桶，则每个桶占用1s的数据，默认`10s`
      + `metrics.rollingPercentile.enabled`：开启执行时间延迟，以及统计功能，按照百分比的方式展示出来，默认`true`
      + `metrics.rollingPercentile.timeInMilliseconds`：类似`metrics.rollingStats.timeInMilliseconds`，按照这个时间段统计一次数据，默认`60s`
      + `metrics.rollingPercentile.numBuckets`：类似`metrics.rollingStats.numBuckets`，默认6
      + `metrics.rollingPercentile.bucketSize`：设置最多有多少桶，如果超出这个值，则开始进行回滚，覆盖最旧的数据，默认`100`
      + `metrics.healthSnapshot.intervalInMilliseconds`：间隔多少时间计算一次是否开启熔断等功能，如果数据量太大，频率太高会消耗太多cup，默认`50ms`
    + `thread configuration`：线城池配置参数
      + 如果有多个功能使用了同一个线程池，那么哪个线程池先初始化成功，则以哪个线程池为标准。如：`hello`业务仅仅配置了一个`threadPoolKey`= `failfast`，
        并且希望使用`failfast`线程池配置的参数，而`failfast`的线程参数信息配置在`world`业务中，如设置了`coreSize=4`等一系列参数。如果客户端先调用`hello`，
        那么将忽略`failfast`线程的参数信息，直接以默认的参数进行线程初始化，仅仅设置线程名为`failfast`。当客户端调用`world`业务时，发现已经存在`failfast`线程池，
        则直接使用当前线程池，将忽略自己创建的线程参数(为了防止此类问题，最好将线程池配置在配置文件中)
      + 线程池名称生产规则
        1. 如果有设置`threadPoolKey`，则线程组名称为`threadPoolKey`
        2. 如果线程有设置`groupKey`，则线程组名称为`groupKey`
        3. 以上都未设置则线程池名称为：`hystrix-className-number`
      + `coreSize`：核心线程数，默认`10`个
      + `maximumSize`：最大线程数，默认`10`，与核心线程数保持一致，保证是固定线程池(注意，此参数默认是不生效的，需要开启`allowMaximumSizeToDivergeFromCoreSize`) 
      + `maxQueueSize`：默认`-1`，表示阻塞队列为`SynchronizedQueue`，即队列为空，所有请求都转交给线程处理
      + `queueSizeRejectionThreshold`：因为`maxQueueSize`不可以动态调整，所以增加了这个参数，如果`maxQueueSize`未达到阙值，而这个参数达到了阙值，也会开始拒绝请求。
         也就是说，`maxQueueSize`和`queueSizeRejectionThreshold`，那个值小就会以哪个值为标准，如果`maxQueueSize=-1`，那么这个参数也无效，默认`5`
      + `keepAliveTimeMinutes`：线程空闲多少时间后进行回收(注意！需要开启`allowMaximumSizeToDivergeFromCoreSize`才生效)
          如果`coreSize<maximumSize`，则超过这个时间则回收线程`>coreSize`的空闲线程，默认`1`分钟
      + `allowMaximumSizeToDivergeFromCoreSize`：是否允许设置`maximumSize`大小，默认`false`

#### hystrix dashboard
  + [demo传送门](http://blog.csdn.net/u012702547/article/details/78224483)
  + 应用层添加`@EnableHystrixDashboard``@EnableTurbine`即开启监控功能
  + 按照如下配置即可
    
        turbine:
          aggregator:
            cluster-config: SPRING-CLOUD-TUTORIAL, SPRING-CLOUD-TUTORIAL-PROVIDER // 需要监控的应用名称，必须大写，多个则以逗号隔开
          appConfig: SPRING-CLOUD-TUTORIAL,SPRING-CLOUD-TUTORIAL-PROVIDER // 名称必须与上面的一致
          instanceUrlSuffix: actuator/hystrix.stream // 默认使用hystrix.stream访问被监控的应用，但是2.x版本的路径改了，所以这里需要修改
      
#### [zipkin：分布式调用链](https://github.com/openzipkin/zipkin)
#### 利用zipkin需要实现的功能
  + 调用链日志与业务日志结合，先全部加入到日志文件中，然后再由分析日志工具将调用链日志重新清洗
  + 高可用，界面渲染非常耗性能
  + 默认采集了http请求的所有日志，还需要采集MQ，数据库等日志
  + 将业务日志与调用链日志结合
  + `MDC.ID`以及与业务绑定，但是在将分布式调用数据发送给服务端时，未打印数据内容，只要将这部分数据打印数据打印出来再配合`log`日志输出即可
  + 调用链日志将会产生两份，一份在日志文件中，一份被写入`zipkin`中，后期将把写入`zipkin`中的日志从日志文件中解析获取
#### zipkin统计界面一些有疑问的现象
  + 按照约定，每一个请求都算是一个`span`，所以客户端(本客户端发送和接收)和服务端(服务端接收然后再响应)都会有一个`span`，所以就出现了一个`RPC`调用两个`span`
  + 在通过浏览器查看真实的`json`数据时，有个非常有趣的现象，客户端的接收时间居然小于服务端发送时间，常理应该是服务端先发送，然后再客户端接收，为什么时间还相反？
     这里是因为发送统计数据时时产生的误差，在服务端接收请求后，会将数据发送给客户端，然后再异步发送数据到`zipkin`，而且获取的时间是当前时间，所以产生了时间差
  + 由于一个`RPC`调用，会产生两个`span`，所以在界面上也会显示两个`span`，而且按照层级嵌套的方式显示。两个`span`生成流程是：
     `master client -> slave client -> slave response -> master response`，`master span`表示这个span需要与服务端一起共用，服务端也需要这个`span`，
      最终产生一个客户端和服务端一起的`span`，可以根据这个`span`计算出客户端耗时，服务端耗时，网络耗时。
      `slave span`仅仅被客户端使用，表示客户端从发送请求到接收总共耗时，根据`slave span`和`master span`可以计算出中间耗时，比如中间还有其它拦截器调用
  + 如果是调用超时，超时拦截器会提前返回数据并生成一个`span`，如果是使用了`ribbon`负载，那么有可能再次请求服务端，所以会再次产生一个`span`，
     所以在调用链界面会看到两个或者多个`span`，而由于服务端最后返回了数据，所以客户端最终的接收时间和服务端发送时间以最后的请求为准
  + 客户端和服务端发送的数据中，都是按照各自的系统时间计算，所以可能会产生一点小小的误差
#### 配置参数
  + `spring.sleuth.sampler.probability=1`，默认仅采集`10%`的数据，我们需要全量采集

#### swagger引入理由
  + 由于接口众多，并且细节复杂（需要考虑不同的HTTP请求类型、HTTP头部信息、HTTP请求内容等），高质量地创建这份文档本身就是件非常吃力的事，下游的抱怨声不绝于耳
  + 随着时间推移，不断修改接口实现的时候都必须同步修改接口文档，而文档与代码又处于两个不同的媒介，除非有严格的管理机制，不然很容易导致不一致现象
  + Swagger2可以轻松的整合到Spring Boot中，并与Spring MVC程序配合组织出强大RESTful API文档。它既可以减少我们创建文档的工作量，同时说明内容又整合入实现代码中，
     让维护文档和修改代码整合为一体，可以让我们在修改代码逻辑的同时方便的修改文档说明。另外Swagger2也提供了强大的页面测试功能来调试每个RESTful API
  + [swagger2传送门](http://springfox.github.io/springfox/docs/current/)

#### 文档要求
  + 因为是`rest api`，所以生成的文档都必须标注为`RestController`
  + 每个`controller`、`method`、`parameter`、`RequestBody`、`ResponseBody`、`Model`、`Model.Field`都必须写上注释
  + 默认是所有的`controller`、`method`都提供文档，如果某个`controller`或者某个`method`不需要提供出来，那么请添加隐藏注解

#### demo
  + `controller`定义
  + ![](http://note.youdao.com/yws/api/personal/file/F210B1A196E34EDC868D7FC129D52CDC?method=download&shareKey=37bdcaa87b73934ed7c67c090358aedd)
  + 入参或者出参对象定义
  + ![](http://note.youdao.com/yws/api/personal/file/2B279C4C1D46489382D4EEB71D0BA37B?method=download&shareKey=15b968bc6ccb8d92d767d44d9257a1b2)
  + 实际效果图
  + ![](http://note.youdao.com/yws/api/personal/file/0604C664DBA14ADEB9A2EA6DB35FD7BA?method=download&shareKey=4f3abea5afa2e2eb593a1268b1241a98)
  + [登录地址](http://localhost:8080/swagger-ui.html)


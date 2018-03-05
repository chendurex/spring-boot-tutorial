### 一、简介
  + 说明：这是一个`springboot`框架，完全采用`springboot`进行开发的框架(version:`1.5.6.RELEASE`)
  + Spring Boot提供了一个快速（和自治的）的方式来创建一个基于Spring的生产环境。它是基于Spring框架，支持约定优于配置，并且被设计成尽可能快地让你启动和运行程序。[传送门](https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/htmlsingle/)
  
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
  + `gradle.build`按如下方式修改(除了修改`pro_name`和`childVersion`其它的保持一致)：
    
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


#### 本地如何方便开发(仅对`idea`用户有效)
  + 在部署项目的时候，让工具自动帮我们构建项目，然后再部署，那这样就不用人为的先通过命令行构建再部署了，然后如下图操作：
     + ![](http://note.youdao.com/yws/api/personal/file/8B6D02A7DA974889B5EF8A5B348918EF?method=download&shareKey=f770d6354fe14b5da38acc35a9305ca6)
     + ![](http://note.youdao.com/yws/api/personal/file/3112775C58DC4EBFA49CB01E7288E7B6?method=download&shareKey=42081233e97198a4429f474ad60026c6)
     + ![](https://note.youdao.com/yws/api/personal/file/72503730C73A44E6B872EF92BB810DE8?method=download&shareKey=aeb363f3bdfc7d6fbc49687f283ae4ec)
     + ![](https://note.youdao.com/yws/api/personal/file/599C23F357D24ADC9A8ECA2BE8A827A9?method=download&shareKey=3600dec50362043cde4c041ea8050636)
  + 如果在构建或者部署时出现类似这样的错误：`Unable to delete file: D:\chen_project\chen-hr-perf\chen-perf-service\build\libs\chen-perf-service-1.0.8-SNAPSHOT.jar`
     那么请大家打开任务管理器，找到顶部的`java.exe`程序，然后选择删除一个试试能不能构建，不能则再删除，直到可以构建为止。
      原因是在大家使用`idea`的命令行或者构建工具时，都会开启一个程序执行，并且会一直保持程序的运行，那如果在先使用命令行执行构建的话，然后再启动项目时，
      因为在启动项目的时候又会构建一次，但是前面构建产生的文件已经被第一个构建所绑定了，所以第二次构建的时候是不能再次操作文件，导致构建失败，所以大家直接部署
      项目就好，不要先构建再部署，防止出现这种不可避免的问题

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
    + 服务与服务调用(`eureka`)
    + 路由和服务网关(`zuul`)
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
  + `eureka`与`zookeeper`对比：
     + `eureka`与`zookeeper`相比，`eureka`是属于拉模式，而`zookeeper`属于推模式，所以在时效上，`eureka`响应相对比`zookeeper`慢，但是也导致`zookeeper`服务端的压力比`eureka`大很多
     + `Zookeeper保证CP` ：当向注册中心查询服务列表时，我们可以容忍注册中心返回的是几分钟以前的注册信息，但不能接受服务直接down掉不可用。也就是说，
        服务注册功能对可用性的要求要高于一致性。但是zk会出现这样一种情况，当master节点因为网络故障与其他节点失去联系时，剩余节点会重新进行leader选举。
        问题在于，选举leader的时间太长，30 ~ 120s, 且选举期间整个zk集群都是不可用的，这就导致在选举期间注册服务瘫痪。在云部署的环境下，
        因网络问题使得zk集群失去master节点是较大概率会发生的事，虽然服务能够最终恢复，但是漫长的选举时间导致的注册长期不可用是不能容忍的
     + `Eureka保证AP` : Eureka看明白了这一点，因此在设计时就优先保证可用性。Eureka各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然可以提供注册和查询服务。
        而Eureka的客户端在向某个Eureka注册或时如果发现连接失败，则会自动切换至其它节点，只要有一台Eureka还在，就能保证注册服务可用(保证可用性)，只不过查到的信息可能不是最新的(不保证强一致性)。
        除此之外，Eureka还有一种自我保护机制，如果在15分钟内超过85%的节点都没有正常的心跳，那么Eureka就认为客户端与注册中心出现了网络故障，此时会出现以下几种情况：
        1. Eureka不再从注册列表中移除因为长时间没收到心跳而应该过期的服务
        2. Eureka仍然能够接受新服务的注册和查询请求，但是不会被同步到其它节点上(即保证当前节点依然可用)
        3. 当网络稳定时，当前实例新的注册信息会被同步到其它节点中
        因此， Eureka可以很好的应对因网络故障导致部分节点失去联系的情况，而不会像zookeeper那样使整个注册服务瘫痪。

  + `eureka`一些可能需要的配置介绍：[所有配置说明传送门](http://cloud.spring.io/spring-cloud-static/Finchley.M2/#_appendix_compendium_of_configuration_properties)
   + `eureka.server.eviction-interval-timer-in-ms` ：服务端自动移除过期的客户端(以毫秒为单位，默认是60秒)，如果在网络不稳定的情况，可以适当提高当前值(以当前时间间隔检查并移除过期的客户端，判断是否过期由`eureka.instance.lease-expiration-duration-in-seconds`确定)
   + `eureka.server.enable-self-preservation` ：是否开启自我保护模式，默认为`true`。默认情况下，如果Eureka Server在一定时间内没有接收到某个微服务实例的心跳，Eureka Server将会注销该实例（默认90秒）。
       但是当网络分区故障发生时，微服务与Eureka Server之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，此时本不应该注销这个微服务。Eureka通过“自我保护模式”来解决这个问题——
       当Eureka Server节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，Eureka Server就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。
       当网络故障恢复后，该Eureka Server节点会自动退出自我保护模式。综上，自我保护模式是一种应对网络异常的安全保护措施。它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微服务都会保留），
       也不盲目注销任何健康的微服务。使用自我保护模式，可以让Eureka集群更加的健壮、稳定。(如果出现当前情况，则会在页面提示：`EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT.
       RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE`)，但是如果真的是突然大量的服务宕机，那么就会出现服务调用大量失败，情况严重可能会导致雪崩效应
    + `spring.application.name` ： 客户端或者服务端显示的唯一名称，用于区别各个业务
    + `eureka.client.serviceUrl.defaultZone` ： 客户端向服务端发送注册请求地址，注意端口号要跟tomcat端口号一致
    + `eureka.client.register-with-eureka` ： 表示是否将自己注册到`Eureka Server`，默认为`true`(如果是服务器，则改为`false`)
    + `eureka.client.fetch-registry` ：  表示是否从`Eureka Server`获取注册信息，默认为`true`(如果是服务器，则改为`false`)
    + `eureka.instance.lease-expiration-duration-in-seconds` ：表示eureka server至上一次收到client的心跳之后，等待下一次心跳的超时时间，在这个时间内若没收到下一次心跳，则将移除该客户端(默认90秒)，如果该值太大，则很可能将流量转发过去的时候，该instance已经不存活了；如果该值设置太小了，则instance则很可能因为临时的网络抖动而被摘除掉；该值至少应该大于`lease-renewal-interval-in-seconds`(内部通过一个`lastUpdateTimestamp`确定，`lastUpdateTimestamp = System.currentTimeMillis() + duration`。注意：这个最后更新时间等于当前时间+过期间隔，而服务端移除客户端时，也是`lastUpdateTimestamp+duration`，所以服务端真正移除客户端的时间公式为：`remove-time<=eureka.server.eviction-interval-timer-in-ms+eureka.instance.lease-expiration-duration-in-seconds*2`)
   + `eureka.instance.lease-renewal-interval-in-seconds` ：客户端发送心跳给服务端的频率。如果在`lease-expiration-duration-in-seconds`后，服务端没有收到客户端的心跳，
        则将移除当前客户端。另外，如果当前客户端实现了`HealthCheckCallback`，并决定让自己`unavailable`的话，则该客户端也不会接收到流量(默认30秒)
     + `eureka.instance.lease-renewal-interval-in-seconds`与`eureka.instance.lease-expiration-duration-in-seconds`是配置在客户端，而不是服务端，也就是说服务端针对每个不同的客户端实现不同的移除策略
   + `eureka.client.registry-fetch-interval-seconds` ：客户端从服务端获取注册信息的频率(默认30秒)，如果消费者对提供者要求比较高的成功率，则降低这个时间
   + `eureka.client.eureka-server-total-connections` ：客户端对所有服务端最大的连接数(默认200)，
   + `eureka.client.eureka-server-total-connections-per-host` ：客户端针对单个服务端最大的连接数(默认50)
   + `eureka.client.eureka-server-read-timeout-seconds` ：客户端发送请求到服务端的超时时间(默认8秒)
   + `eureka.client.eureka-server-connect-timeout-seconds` ：客户端与服务端建立连接的超时时间(默认5秒)
   + `eureka.client.eureka-connection-idle-timeout-seconds` ：客户端与服务端建立的连接在某个间隔时间未使用后自动回收(默认30秒)

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

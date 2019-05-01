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

#### 基本功能
 + 创建启动类，并且增加`@SpringBootStarterAnnotation`注解，可以参照`com.spring.boot.tutorial.Application`实现
  + 在开发和测试环境，如果觉得随机端口比较麻烦，可以在配置中心增加server.port=xxxx固定端口号
 + 日志配置规则
  + 默认情况下，`windows`系统(即本地开发环境)只会在控制台打印日志，不会打印日志到日志文件，如果需要将日志文件打印到日志
  文件，请在配置中心增加如下配置：`logging.custom.std.file.enabled=true`
  + 默认情况下，除`windows`系统(即开发、测试、预发、线上)只会在日志文件打印日志，不会将日志打印到控制台，如果需要将
  日志文件打印到控制台，请在配置中心增加如下配置：`logging.custom.std.console.enabled=true`  
  + 动态修改某个日志级别，如修改`com.spring.boot.tutorial.service`项目包路径下日志级别为`debug`，则去配置中心中，在自己的项目增加配置为：
    `logging.level.com.spring.boot.tutorial.service=debug`，注意前面带了`logging.level`前缀
  + 如果某个日志级别监控完毕，需要改回原来的默认日志级别，则删除当前配置即可
  + 线上默认开启了监控所有的入参操作，如果需要关闭，请在配置中心修改日志等级即可：
    + `logging.level.com.spring.boot.sc.advice.CustomizePathLogInterceptor=warn`
    + `logging.level.com.spring.boot.sc.advice.CustomizeReqInterceptor=warn`
  + 线上也提供了监控参数返回值的功能，默认是关闭的，如果需要开启，可以在配置中心修改日志等级：
    + `logging.level.com.spring.boot.sc.advice.CustomizeResInterceptor=debug` 
  + 默认只开启了`com.spring.boot`路径日志，级别为`info`，这个最基本的日志是不能修改或者删除，如果需要覆盖日志，那么需要在配置
    中心增加其它的策略，如需要将本项目的所有日志改为`debug`，则可以在配置中心增加`logging.level.com.spring.boot.tutorial=debug`
    配置，或者仅仅针对某个包下面修改配置，如`logging.level.com.spring.boot.tutorial.service=debug`。如果不需要当前日志策略，
    则删除配置即可。
  + 注意！如果开启了整个项目的日志级别为`debug`，那么会导致项目的日志量非常多，特别是`mybatis`的`sql`语句也会打印，如果仅仅是
    单独针对某个`mybatis`打印`sql`语句，那么配置`logging.level.xxx.mapper.xxxMapper=debug`即可，千万不可随意开启整个项目的
    日志配置为`debug`，用完后，一定要记得删除配置
    
 + 增加了`com.spring.boot.sc.SpringContextHolder`工具类去获取`bean`
 + 增加了事务拦截配置切面，默认拦截`com.spring.boot..*.service.impl`切面( 也可以通过在配置中心修改默认配置，
   如`spring.transaction.pointcut.expression=execution(* com.spring.boot..*.service.impl.*.*(..))` )，
   + 如果方法有配置事务注解，则以注解为标准，否则以很切面的事务为标准
   + 针对`select、query、find、get、list`等默认是不开启事务，`readonly`
   + 针对事务配置，大家可以直接查看源码`com.spring.boot.sc.spring.TransactionConfiguration`
   + 如果需要动态增加一个事务拦截切面，那么可以在配置中心增加配置，规则为：`spring.transaction.interceptor.include.xxx=config`
     + `spring.transaction.interceptor.include.fire=fire*|PROPAGATION_NOT_SUPPORTED, readOnly` # 表示增加一个名称为fire配置，
     拦截以fire开头的方法名，事务属性为只读事务
   + 如果需要删除默认的事务拦截切面，那么可以在配置中心增加配置，规则为：`spring.transaction.interceptor.include.xxx=true`
     + `spring.transaction.interceptor.exclude.select=true` #表示删除名称为`select`的事务切面
   + 默认不开启事务，也就是默认大家都是裸奔，如果需要开启事务请在`apollo`配置`spring.transaction.enabled=true`
   + 注意：此事务拦截功能只是在原来`spring`提供的事务拦截功能的基础上增加了一个事务横切面而已，方便大家不用每次为了开启事务
   都需要在方法上面增加`Transactional`注解，它并未影响到我们通过`Transactional`注解显示的开启某个方法注解。如果同时开启事务功能，
   并且在方法上面也存在`Transactional`注解，那么会以`Transactional`注解为首选，没有`Transactional`注解时，此功能才成为备选方案
   注解开启事务功能
 + 提供编译时检查代码功能        
#### 如何扩展配置
  + 实现方式都是通过添加`Configuration`注解以代表一个配置，但是启动时有两种方式
    + 第一种：`Configuration`是基于`Component`注解，所以只要配置的扫描路径能扫描到，即可实现注入配置
    + 第二种：通过`spring boot`约定的方式，将配置加入到`spring.factories`文件中(具体可以查看上文提到的：如何开发一个基于boot的组件)，
              官网推荐这种方式，因为`Configuration`就是代表一个配置，既然是配置，那么应该遵循配置的约定(类似XML文件)，这样使用者会非常清楚当前架构的设计，
              而且采用这种方式可以控制配置生成的顺序，而采用注解是无法实现的
  + 如果需要控制顺序，那么请在`Configuration`配置文件中使用`AutoConfigureOrder`、`AutoConfigureAfter`、`AutoConfigureBefore`等属性
  + 如果将配置放入配置中心，则一定要加上`模块`，不然会导致所有的项目生效，出现大面积系统故障
  + 如果需要控制生成的条件，那么请使用`ConditionalOn*`等注解，[实现机制可以看这里](https://dzone.com/articles/how-springboot-autoconfiguration-magic-works)

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

#### 多数据源使用
  + 单数据源使用时直接在配置文件中增加账号密码即可开箱即用
    + spring.druid.username = username
    + spring.druid.password = password
    + spring.druid.url = jdbc:mysql://url:port/database?characterEncoding=utf8
  + 多数据源使用方式(超过2个以上数据源的使用方式)
    + 提供两种数据源操作方式
      + 手动切换数据源，使用完毕后需要手动切换默认数据源(可以针对多次db操作)
      + 自动切换数据源，使用完毕后自动切换到默认数据源(仅针对一次db操作)
    + `spring.druid.checkMultiName = true` # 开启检查数据源功能，如果数据源切换有误则抛出异常
    + 首先在配置中开启多数据源：`spring.druid.multi-datasource.enabled=true`
    + 然后再配置多数据源账号密码，比如我可能需要连接`abc`的数据库， 那么我将`{name}`占位符改为`abc`，
    即`spring.druid.multi.abc.username=dbname`；如果我还要继续连接`edf`数据库，那么我再配置一份
    `spring.druid.multi.def.username=dbname`即可，依次类推，业务方在获取其它数据源的时候，
    通过`MultipleDataSource.route({name})` 即可获取到需要的数据源
    + spring.druid.multi.{name}.username=dbname
    + spring.druid.multi.{name}.password=dbpwd
    + spring.druid.multi.{name}.url=jdbc:mysql://host:port/db?characterEncoding=utf8
    + 示例
    
                @Test
                public void testMulti2() {
                    // 切换到第一个数据源
                    MultipleDataSource.routeAtMulti("spring");
                    Assert.assertNull(userMapper.select(id));
                    userMapper.insert(user);
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    // 切换到第二个数据源
                    MultipleDataSource.routeAtMulti("cloud");
                    Assert.assertNull(userMapper.select(id));
                    userMapper.insert(user);
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    userMapper.delete(id);
                    Assert.assertNull(userMapper.select(id));
            
                    MultipleDataSource.routeAtMulti("spring");
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    userMapper.delete(id);
                    Assert.assertNull(userMapper.select(id));
            
                    // 回到默认数据源
                    MultipleDataSource.reset();
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    userMapper.delete(id);
                    Assert.assertNull(userMapper.select(id));
                }
    
               @Test
               public void testMultiAtOnce() {
                   int count = userMapper.insert(user);
                   Assert.assertTrue(count > 0);
                   int id = user.getId();
                   Assert.assertEquals(v, userMapper.select(id).getName());
           
                   // 切换数据源
                   MultipleDataSource.routeAtOnce("spring");
                   Assert.assertNull(userMapper.select(id));
                   // 回到默认数据库中，所以可以查到数据
                   Assert.assertEquals(v, userMapper.select(id).getName());
                   MultipleDataSource.routeAtOnce("spring");
                   userMapper.insert(user);
                   // 删除默认数据库中数据
                   userMapper.delete(id);
                   Assert.assertNull(userMapper.select(id));
                   MultipleDataSource.routeAtOnce("spring");
                   Assert.assertEquals(v, userMapper.select(id).getName());
                   MultipleDataSource.routeAtOnce("spring");
                   userMapper.delete(id);
                   MultipleDataSource.routeAtOnce("spring");
                   Assert.assertNull(userMapper.select(id));
               }
               
               
  + 主从使用方式(其实就是简化版的多数据源)  
    + 跟上面的配置一模一样，只是数据源不能配置超过两个，否则无法识别从库
    + 示例
    
                @Test
                public void testMasterSlaveAtOnce() {
                    user.setId(i);
                    int count = userMapper.insert(user);
                    Assert.assertTrue(count > 0);
                    int id = user.getId();
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    // 切换到从数据库,注意，只执行一次DB操作默认会回到主库
                    MultipleDataSource.slaveAtOnce();
                    Assert.assertNull(userMapper.select(id));
                    // 回到默认数据库了，所以能继续查到数据
                    Assert.assertNotNull(userMapper.select(id));
                    MultipleDataSource.slaveAtOnce();
                    userMapper.insert(user);
                    MultipleDataSource.slaveAtOnce();
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    // 删除主库数据
                    userMapper.delete(id);
                    Assert.assertNull(userMapper.select(id));
                    MultipleDataSource.reset();
                    Assert.assertNull(userMapper.select(id));
                    MultipleDataSource.slaveAtOnce();
                    userMapper.delete(id);
                    MultipleDataSource.slaveAtOnce();
                    Assert.assertNull(userMapper.select(id));
                }
                
                @Test
                public void testMasterSlaveMulti() {
                    // 切换到从数据库，不会做自动切换，需要人工手动切换
                    MultipleDataSource.slaveAtMulti();
                    Assert.assertNull(userMapper.select(id));
                    userMapper.insert(user);
                    Assert.assertEquals(v, userMapper.select(id).getName());
                    userMapper.delete(id);
                    Assert.assertNull(userMapper.select(id));
                }
                
  + [spring-boot-mybatis](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
  + [druid](https://github.com/alibaba/druid)
  + [spring-boot-pagehelp](https://github.com/abel533/MyBatis-Spring-Boot)
                
### [redis-cluster](https://github.com/chendurex/spring-boot-tutorial/tree/master/redis-spring-boot-starter)
                
### spring cloud简介
  + `Spring Cloud`为开发人员提供了快速构建分布式系统中一些常见模式的工具（例如配置管理，服务发现，断路器，智能路由，微代理，控制总线）。
    分布式系统的协调导致了样板模式, 使用`Spring Cloud`开发人员可以快速地支持实现这些模式的服务和应用程序。他们将在任何分布式环境中运行良好，
    包括开发人员自己的笔记本电脑，裸机数据中心，以及`Cloud Foundry`等托管平台
  + 以下为`Spring Cloud`的核心特性：
    + [服务注册和发现(eureka)](https://github.com/chendurex/chen-spring-boot-starter/wiki/EUREKA)
    + 服务与服务调用(`feign`)
    + 路由和服务网关(`zuul`/`spring cloud api gateway`)
    + 负载均衡(`ribbon`)
    + [断路器(hystrix)](https://github.com/chendurex/chen-spring-boot-starter/wiki/hystrix)
    + 分布式链路跟踪器(`sleuth`/`zipkin`)
  + 为什么考虑Spring Cloud
    + Spring Cloud来源于Spring，质量、稳定性、持续性都可以得到保证
    + Spirng Cloud天然支持Spring Boot，更加便于业务落地
    + Spring Cloud发展非常的快，从16年开始的时候相关组件版本为1.x，到现在将要发布2.x系列
    + Spring Cloud是Java领域最适合做微服务的框架
    + 相比于其它框架,Spring Cloud对微服务周边环境的支持力度最大
    + 对于中小企业来讲，使用门槛较低

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
  + 文档要求  
    + 因为是`rest api`，所以生成的文档都必须标注为`RestController`
    + 每个`controller`、`method`、`parameter`、`RequestBody`、`ResponseBody`、`Model`、`Model.Field`都必须写上注释
    + 默认是所有的`controller`、`method`都提供文档，如果某个`controller`或者某个`method`不需要提供出来，那么请添加隐藏注解
  + 基本的注解说明
    + @ApiModel("SWAGGER自定义对象"):描述DTO对象，名称必须唯一
    + @ApiModelProperty(notes = "用户ID", example = "1", required = true):描述DTO对象中的属性域
    + @Api(description = "SWAGGER 介绍"):描述API接口集
    + @ApiOperation(value = "SWAGGER介绍", response = UserVO.class):描述API单个接口
    + 如果需要对某个请求增加头信息，可以直接在controller中增加header即可
            
            @ApiOperation(value = "commons", response = ResResult.class)
            @PostMapping("/commons")
            public ResResult<SwaggerDTO> timeout(@RequestBody @Validated SwaggerDTO swaggerDTO,@RequestHeader("uid") Integer uid) {
                return ResUtils.data(swaggerDTO);
            }
  
   + 如果需要针对整个项目所有的接口都增加header，可以通过在配置中心增加配置项，`headerName1,type,defaultValue1|headerName2,type,defaultValue2|etc...`
     + headerName1代表第一个头部名称
     + type代表头部类型，有string、int、boolean等基本类型
     + defaultValue1代表默认值，如果不需要则不填写(包括前面的逗号也移除)
     + 每个不同的头部信息通过|(竖线)分割，每个头部不同的属性通过,(半角逗号隔开)
  + 其它的注解请自行查看官网
     
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


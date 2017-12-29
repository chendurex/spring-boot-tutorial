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

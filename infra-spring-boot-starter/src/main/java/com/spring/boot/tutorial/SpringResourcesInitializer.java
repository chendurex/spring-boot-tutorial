package com.spring.boot.tutorial;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author chendurex
 * @description 注入配置属性(这个是在没有配置中心的情况加载本地或者项目资源文件，如果需要更加个性化的配置可以参考) {@link EurekaInstanceHostname}
 *     这里默认分为线上与开发两种配置，我通过{@link ConditionalOnProperty#havingValue()} 来匹配处理的
 *     启动项目的时候通过-Dcom.jianlier.boot.pro=true则表示生产，改为false则为开发
 *     注意，因为开发环境每次配置启动变量比较麻烦，所以我增加了一个matchIfMissing属性来控制默认匹配规则(matchIfMissing作为未匹配的备选方案，
 *     而不是与havingValue作为互斥条件，理解这点很重要，它是前置匹配失败后，然后再次判断这个key是否存在，如果true则表示key不存在则执行，否则不执行)
 *     而且，另外还增加了havingValue=false这个匹配条件，如果不加的话，havingValue默认值是""，而文档介绍""是可以匹配到除false意外的任何值，
 *     这样导致了只要我设置了变量就可以匹配成功，所以默认不能让它匹配到设置的值，这样就变成了匹配除false意外的任何条件了
 *     在没有匹配到任何条件的情况，matchIfMissing就可以作为备选条件来再次检查是否有这个key，如果没有这个key则执行条件
 * @date 2018-04-01 01:50
 *   如果有配置中心，这个部分就不需要了
 * @update 2019-03-12
 */
public class SpringResourcesInitializer {

    @Configuration
    @ConditionalOnProperty(prefix = "com.spring.boot.tutorial", name = "pro", havingValue = "true")
    @PropertySource(value = "file:/usr/local/tools/config.properties", encoding="UTF-8")
    class ProductConfig {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "com.spring.boot.tutorial", name = "pro", havingValue = "false", matchIfMissing = true)
    @PropertySource(value = "file:/usr/local/src/tools/local.properties", encoding="UTF-8")
    class DevConfig {
    }
}

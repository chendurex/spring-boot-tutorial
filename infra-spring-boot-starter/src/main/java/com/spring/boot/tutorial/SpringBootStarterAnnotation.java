package com.spring.boot.tutorial;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;

import java.lang.annotation.*;

/**
 * 业务方使用封装好的注解，直接启动项目
 * <p>
 *     默认排除的依赖：
 *     RedisAutoConfiguration: 我们项目采用了自己的redis配置，而且是采用自己使用的方式，所以排除boot提供的配置<br>
 *     RedisRepositoriesAutoConfiguration: 没有使用redis仓库，不需要<br>
 *     MongoAutoConfiguration：目前组件也是采用了自己实现的方式<br>
 *     TransactionAutoConfiguration：采用自己个性化的方式配置事务<br>
 * </p>
 * <p>
 *     使用 {@code MapperScan} 替换原来的{@code MapperScannerConfigurer} 来扫描mapper文件
 *         因为MapperScannerConfigurer是一个bean，所以只有在初始化的时候才会动态的初始化mapper bean，这里就需要控制这个bean在未扫描component服务的时候，
 *         先实例化当前bean，以前以配置文件的形式确实不会出现问题，但是现在改为boot的方式后，所有的配置都是以bean的方式存在，所以有可能出现配置比普通的component
 *         还后实例化，导致出现了无法注入bean的问题。改为mapperScan注解后，它会在bean未开始实例化的时候先扫描对应的mapper文件，然后生成初始的bean definition
 *         然后再由spring 容器去自动初始化
 *
 * </p>
 * @author chen
 * date 2018/8/17 20:19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@EnableFeignClients(basePackages = {com.spring.boot.tutorial})// 开启feignClient扫描
// application默认的项目私有配置、private为线上私有的重要配置，只能由管理员查看、public-default为公共配置
// 如果业务有需要还需增加其它的私有配置，那么需要业务开发人员自行增加扫描配置
@EnableApolloConfig({"application","private", "public-default"})
@SpringBootApplication(
        exclude = {
                RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class, MongoAutoConfiguration.class, TransactionAutoConfiguration.class
        },

        scanBasePackages = {"com.spring.boot.tutorial"})
@MapperScan(value = {"com.spring.boot.tutorial.**.mapper"})
public @interface SpringBootStarterAnnotation {
}

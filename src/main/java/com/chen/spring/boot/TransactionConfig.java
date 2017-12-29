package com.chen.spring.boot;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.CompositeTransactionAttributeSource;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author chen
 * date 2017/9/28 16:26
 */

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class TransactionConfig {

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionInterceptor transactionInterceptor(DataSourceTransactionManager dataSourceTransactionManager) {
        Properties defaultTransactionAttr = new Properties();
        defaultTransactionAttr.setProperty("select*", "PROPAGATION_NOT_SUPPORTED, readOnly");
        defaultTransactionAttr.setProperty("query*", "PROPAGATION_NOT_SUPPORTED, readOnly");
        defaultTransactionAttr.setProperty("find*", "PROPAGATION_NOT_SUPPORTED, readOnly");
        defaultTransactionAttr.setProperty("get*", "PROPAGATION_NOT_SUPPORTED, readOnly");
        defaultTransactionAttr.setProperty("list*", "PROPAGATION_NOT_SUPPORTED, readOnly");
        defaultTransactionAttr.setProperty("executeQueryForList*", "PROPAGATION_NOT_SUPPORTED");
        defaultTransactionAttr.setProperty("executeQueryForInt*", "PROPAGATION_NOT_SUPPORTED");
        defaultTransactionAttr.setProperty("executeForObjectPreparedStatement*", "PROPAGATION_NOT_SUPPORTED");
        defaultTransactionAttr.setProperty("executeForListPreparedStatement*", "PROPAGATION_NOT_SUPPORTED");
        defaultTransactionAttr.setProperty("*NoTx", "PROPAGATION_NOT_SUPPORTED");
        defaultTransactionAttr.setProperty("*", "PROPAGATION_REQUIRED,-Exception");
        NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
        tas.setProperties(defaultTransactionAttr);
        // 配置组合事务，包括声明式和注解方式，默认先检测注解，注解没找到再使用声明式
        return new TransactionInterceptor(dataSourceTransactionManager,
                new CompositeTransactionAttributeSource(new TransactionAttributeSource[]{new AnnotationTransactionAttributeSource(), tas}));
    }

    @Bean
    public Advisor defaultPointcutAdvisor(TransactionInterceptor transactionInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.chen.web.module..*.service.impl.*.*(..)) " +
                "and !execution(* com.chen.web.module..*.engine.service.impl.*.*(..))");
        return new DefaultPointcutAdvisor(pointcut, transactionInterceptor);
    }
}

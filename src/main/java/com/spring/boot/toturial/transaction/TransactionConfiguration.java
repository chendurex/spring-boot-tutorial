package com.spring.boot.toturial.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author cheny.huang
 * @date 2018-08-20 10:38.
 */
@Configuration
@ConditionalOnProperty(value = "spring.transaction.enabled", havingValue = "true")
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class TransactionConfiguration {

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "spring.transaction.interceptor")
    class TransactionAttrIncludedProperties {
        private Map<String, String> include = new HashMap<>();
        Map<String, String> getInclude() {
            return include;
        }
        public void setInclude(Map<String, String> include) {
            this.include = include;
        }
    }

    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "spring.transaction.interceptor")
    class TransactionAttrExcludeProperties {
        private Map<String, String> exclude = new HashMap<>();
        Map<String, String> getExclude() {
            return exclude;
        }
        public void setExclude(Map<String, String> exclude) {
            this.exclude = exclude;
        }
    }

    @Bean
    public TransactionInterceptor transactionInterceptor(TransactionAttrIncludedProperties includedProperties,
                                                         TransactionAttrExcludeProperties excludeProperties,
                                                         DataSourceTransactionManager dataSourceTransactionManager) {
        NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
        tas.setProperties(filter(includedProperties, excludeProperties));
        return new TransactionInterceptor(dataSourceTransactionManager,
                new CompositeTransactionAttributeSource(new TransactionAttributeSource[]{new AnnotationTransactionAttributeSource(), tas}));
    }

    private Properties filter(TransactionAttrIncludedProperties includedProperties, TransactionAttrExcludeProperties excludeProperties) {
        log.info("init transaction interceptor config, includes:{},excludes:{}", includedProperties.getInclude(), excludeProperties.getExclude());
        Map<String, String> all = new HashMap<>(includedProperties.getInclude());
        // 追加默认配置
        appendDefaultTransactionAttr(all);
        // 移除排除项配置
        excludeProperties.getExclude().keySet().forEach(all::remove);
        Map<String, String> f = all.values().stream()
                .filter(v -> v.contains("|"))
                .collect(Collectors.toMap(v -> v.substring(0, v.indexOf("|")), v -> v.substring(v.indexOf("|")+1)));
        Properties defaultTransactionAttr = new Properties();
        defaultTransactionAttr.putAll(f);
        return defaultTransactionAttr;
    }

    /**
     * 其实可以使用方法表达式作为key的，但是spring不支持这种注入方式，它会把key中的[*]符号移除，所以单独取个名字了
     * @param m
     */
    private void appendDefaultTransactionAttr(Map<String, String> m) {
        m.putIfAbsent("select", "select*|PROPAGATION_NOT_SUPPORTED, readOnly");
        m.putIfAbsent("query", "query*|PROPAGATION_NOT_SUPPORTED, readOnly");
        m.putIfAbsent("find", "find*|PROPAGATION_NOT_SUPPORTED, readOnly");
        m.putIfAbsent("get", "get*|PROPAGATION_NOT_SUPPORTED, readOnly");
        m.putIfAbsent("list", "list*|PROPAGATION_NOT_SUPPORTED, readOnly");
        m.putIfAbsent("NoTx", "*NoTx|PROPAGATION_NOT_SUPPORTED");
        m.putIfAbsent("default", "*|PROPAGATION_REQUIRED,-Exception");
    }

    @Bean
    public Advisor defaultPointcutAdvisor(
            @Value("${spring.transaction.pointcut.expression:execution(* com.to8to..*.service.impl.*.*(..))}")String expression,
                                                      TransactionInterceptor transactionInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);
        return new DefaultPointcutAdvisor(pointcut, transactionInterceptor);
    }
}

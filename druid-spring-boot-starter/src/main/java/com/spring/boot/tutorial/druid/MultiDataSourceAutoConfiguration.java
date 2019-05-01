package com.spring.boot.tutorial.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author cheny.huang
 * @date 2018-11-15 10:49.
 */
@ConditionalOnClass(value = {DruidDataSource.class, DruidAutoConfiguration.class})
@EnableConfigurationProperties(value = {MultiDataSourceProperties.class, DruidProperties.class})
@ConditionalOnProperty(value = "spring.druid.multi-datasource.enabled", havingValue = "true")
public class MultiDataSourceAutoConfiguration {
    private static final String DEFAULT_DATASOURCE = "default";
    private static final int MASTER_SLAVE_THRESHOLD = 2;

    @Bean
    DruidDataSourceFactory druidDataSourceFactory(DruidProperties druidProperties, MultiDataSourceProperties multiDataSourceProperties) {
        Map<String ,DataSource> multipleDataSource = initMultipleDruidDataSource(druidProperties, multiDataSourceProperties) ;
        return new DruidDataSourceFactory() {
            @Override
            public DataSource fetch(String name) {
                DataSource dataSource = multipleDataSource.get(name);
                if (dataSource == null) {
                    throw new RuntimeException("数据源不存在["+name+"]");
                }
                return dataSource;
            }

            @Override
            public Set<String> names() {
                return multipleDataSource.keySet();
            }
        };
    }

    @Bean
    public DataSource dataSource(DruidDataSourceFactory druidDataSourceFactory, DruidProperties druidProperties) {
        Map<Object ,Object> multipleDataSource = new HashMap<>(4);
        for (String s : druidDataSourceFactory.names()) {
            multipleDataSource.put(s, druidDataSourceFactory.fetch(s));
        }
        String slave = null;
        if (multipleDataSource.size() == MASTER_SLAVE_THRESHOLD) {
            slave = multipleDataSource.keySet().stream()
                    .filter(s->(!DEFAULT_DATASOURCE.equals(s))).findFirst().orElseThrow(IllegalArgumentException::new).toString();
        }
        MultipleDataSource multiDataSource =  new MultipleDataSource(DEFAULT_DATASOURCE, slave,
                druidDataSourceFactory.names(), druidProperties.getCheckMultiName());
        multiDataSource.setDefaultTargetDataSource(multipleDataSource.get(DEFAULT_DATASOURCE));
        multiDataSource.setTargetDataSources(multipleDataSource);
        return multiDataSource;
    }

    @Bean
    public HandlerInterceptor cleanSlaveDataSourceAfterCompletion() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                return true;
            }

            @Override
            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                MultipleDataSource.cleanAfterCompletion();
            }
        };
    }

    @Bean
    public DataSourceCleanerAspect dataSourceCleanerAspect() {
        return new DataSourceCleanerAspect();
    }

    private Map<String ,DataSource> initMultipleDruidDataSource(DruidProperties druidProperties, MultiDataSourceProperties multiDataSourceProperties){
        Map<String ,DataSource>  dataSourceMap = new HashMap<>(8) ;
        dataSourceMap.put(DEFAULT_DATASOURCE, new SingleDataSource().buildDataSource(druidProperties)) ;
        for (Map.Entry<String, Map<String, String>> stub : multiDataSourceProperties.getMulti().entrySet()) {
            String k = stub.getKey();
            if (DEFAULT_DATASOURCE.equals(k)) {
                throw new RuntimeException("多数据源名称不能用[default]，这个名字以及被设置为默认的名字");
            }
            Map<String, String> v = stub.getValue();
            DruidProperties copied = druidProperties.copy();
            copied.setUsername(v.get("username"));
            copied.setPassword(v.get("password"));
            copied.setUrl(v.get("url"));
            dataSourceMap.put(k, new SingleDataSource().buildDataSource((copied)));
        }
        return dataSourceMap;
    }
}

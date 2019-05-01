package com.spring.boot.toturial.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:27
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties(DruidMonitorProperties.class)
@ConditionalOnProperty(prefix = "spring.druid.monitor",havingValue = "enabled",value = "enabled")
@AutoConfigureAfter(DataSource.class)
public class DruidMonitorAutoConfiguration {
    private final DruidMonitorProperties druidMonitorProperties;

    public DruidMonitorAutoConfiguration(DruidMonitorProperties druidMonitorProperties) {
        this.druidMonitorProperties = druidMonitorProperties;
    }
    @Bean
    public ServletRegistrationBean druidStatViewServlet(){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean<>(
                new StatViewServlet(), druidMonitorProperties.getDruidStatView());
        //白名单：
        servletRegistrationBean.addInitParameter("allow", druidMonitorProperties.getAllow());
        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
        servletRegistrationBean.addInitParameter("deny", druidMonitorProperties.getDeny());
        //登录查看信息的账号密码.
        servletRegistrationBean.addInitParameter("loginUsername", druidMonitorProperties.getLoginUsername());
        servletRegistrationBean.addInitParameter("loginPassword", druidMonitorProperties.getLoginPassword());
        //是否能够重置数据.
        servletRegistrationBean.addInitParameter("resetEnable", druidMonitorProperties.getResetEnable());
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean druidStatFilter(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean<>(new WebStatFilter());
        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns(druidMonitorProperties.getDruidWebStatFilter());
        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter("exclusions", druidMonitorProperties.getExclusions());
        return filterRegistrationBean;
    }
    
}

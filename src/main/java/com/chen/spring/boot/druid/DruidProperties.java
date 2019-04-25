package com.chen.spring.boot.druid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Properties;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:27
 */
@ConfigurationProperties(prefix = "spring.druid")
public class DruidProperties implements Cloneable{
    private final Logger log = LoggerFactory.getLogger(DruidProperties.class);
    private String url;
    private String username;
    private String password;
    private String driverClass;
    private String filters;
    private String connectionProperties;
    private String validationQuery;

    // 是否开启数据源检查功能，默认关闭，表示路由到错误的数据源则使用默认数据源
    private Boolean checkMultiName = Boolean.FALSE;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean poolPreparedStatements;

    private Integer maxWait;
    private Integer maxActive;
    private Integer minIdle;
    private Integer initialSize;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeoutMillis;
    private Boolean logAbandoned;
    private Collection<Object> connectionInitSqls;
    private Properties connProp;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public Boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    public Boolean getRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(Boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Collection<Object> getConnectionInitSqls() {
        return connectionInitSqls;
    }

    public void setConnectionInitSqls(Collection<Object> connectionInitSqls) {
        this.connectionInitSqls = connectionInitSqls;
    }

    public Integer getRemoveAbandonedTimeoutMillis() {
        return removeAbandonedTimeoutMillis;
    }

    public void setRemoveAbandonedTimeoutMillis(Integer removeAbandonedTimeoutMillis) {
        this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
    }

    public Boolean getCheckMultiName() {
        return checkMultiName;
    }

    public void setCheckMultiName(Boolean checkMultiName) {
        this.checkMultiName = checkMultiName;
    }

    public Boolean getLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(Boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public Properties getConnProp() {
        return connProp;
    }

    public void setConnProp(Properties connProp) {
        this.connProp = connProp;
    }

    public DruidProperties copy() {
        try {
            Object o = this.clone();
            DruidProperties copy = (DruidProperties)o;
            copy.setUrl(null);
            copy.setPassword(null);
            copy.setUsername(null);
            return copy;
        } catch (CloneNotSupportedException e) {
            // ignore
            throw new RuntimeException(e);
        }
    }
}

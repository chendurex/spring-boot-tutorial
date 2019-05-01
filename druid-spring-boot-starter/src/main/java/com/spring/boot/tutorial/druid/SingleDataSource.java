package com.spring.boot.tutorial.druid;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

/**
 * @author cheny.huang
 * @date 2018-11-15 10:55.
 */
class SingleDataSource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    DruidDataSource buildDataSource(DruidProperties druidProperties) {
        DruidDataSource dataSource = new DruidDataSource();
        valid(druidProperties);
        dataSource.setUrl(druidProperties.getUrl());
        dataSource.setDriverClassName(druidProperties.getDriverClass());
        dataSource.setUsername(druidProperties.getUsername());
        dataSource.setPassword(druidProperties.getPassword());
        StringBuilder initialParams = new StringBuilder();
        if (hasValue(druidProperties.getInitialSize())) {
            dataSource.setInitialSize(druidProperties.getInitialSize());
            initialParams.append("initialSize : ").append(druidProperties.getInitialSize()).append(",");
        }
        if (hasValue(druidProperties.getMinIdle())) {
            dataSource.setMinIdle(druidProperties.getMinIdle());
            initialParams.append("minIdle : ").append(druidProperties.getMinIdle()).append(",");
        }
        if (hasValue(druidProperties.getMaxActive())) {
            dataSource.setMaxActive(druidProperties.getMaxActive());
            initialParams.append("maxActive : ").append(druidProperties.getMaxActive()).append(",");
        }
        if (hasValue(druidProperties.getTestOnBorrow())) {
            dataSource.setTestOnBorrow(druidProperties.getTestOnBorrow());
            initialParams.append("testOnBorrow : ").append(druidProperties.getTestOnBorrow()).append(",");
        }
        if (hasValue(druidProperties.getMaxWait())) {
            dataSource.setMaxWait(druidProperties.getMaxWait());
            initialParams.append("maxWait : ").append(druidProperties.getMaxWait()).append(",");
        }
        if (hasValue(druidProperties.getTimeBetweenEvictionRunsMillis())) {
            dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
            initialParams.append("timeBetweenEvictionRunsMillis : ").append(druidProperties.getTimeBetweenEvictionRunsMillis()).append(",");
        }
        if (hasValue(druidProperties.getMinEvictableIdleTimeMillis())) {
            dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
            initialParams.append("minEvictableIdleTimeMillis : ").append(druidProperties.getMinEvictableIdleTimeMillis()).append(",");
        }
        if (hasValue(druidProperties.getValidationQuery())) {
            dataSource.setValidationQuery(druidProperties.getValidationQuery());
            initialParams.append("validationQuery : ").append(druidProperties.getValidationQuery()).append(",");
        }
        if (hasValue(druidProperties.getTestWhileIdle())) {
            dataSource.setTestWhileIdle(druidProperties.getTestWhileIdle());
            initialParams.append("testWhileIdle : ").append(druidProperties.getTestWhileIdle()).append(",");
        }
        if (hasValue(druidProperties.getTestOnReturn())) {
            dataSource.setTestOnReturn(druidProperties.getTestOnReturn());
            initialParams.append("testOnReturn : ").append(druidProperties.getTestOnReturn()).append(",");
        }
        if (hasValue(druidProperties.getPoolPreparedStatements())) {
            dataSource.setPoolPreparedStatements(druidProperties.getPoolPreparedStatements());
            initialParams.append("poolPreparedStatements : ").append(druidProperties.getPoolPreparedStatements()).append(",");
        }
        if (hasValue(druidProperties.getMaxPoolPreparedStatementPerConnectionSize())) {
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
            initialParams.append("maxPoolPreparedStatementPerConnectionSize : ").append(druidProperties.getMaxPoolPreparedStatementPerConnectionSize()).append(",");
        }
        if (hasValue(druidProperties.getFilters())) {
            initialParams.append("filters : ").append(druidProperties.getFilters()).append(",");
            try {
                dataSource.setFilters(druidProperties.getFilters());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (hasValue(druidProperties.getConnectionProperties())) {
            dataSource.setConnectionProperties(druidProperties.getConnectionProperties());
            initialParams.append("connectionProperties : ").append(druidProperties.getConnectionProperties()).append(",");
        }
        if (hasValue(druidProperties.getRemoveAbandoned())) {
            dataSource.setRemoveAbandoned(druidProperties.getRemoveAbandoned());
            initialParams.append("removeAbandonProperties : ").append(druidProperties.getRemoveAbandoned()).append(",");
        }
        if (hasValue(druidProperties.getRemoveAbandonedTimeoutMillis())) {
            dataSource.setRemoveAbandonedTimeoutMillis(druidProperties.getRemoveAbandonedTimeoutMillis());
            initialParams.append("removeAbandonedTimeoutMillis : ").append(druidProperties.getRemoveAbandonedTimeoutMillis()).append(",");
        }
        if (hasValue(druidProperties.getLogAbandoned())) {
            dataSource.setLogAbandoned(druidProperties.getLogAbandoned());
            initialParams.append("logAbandoned: ").append(druidProperties.getLogAbandoned()).append(",");
        }
        if (hasValue(druidProperties.getConnectionInitSqls())) {
            dataSource.setConnectionInitSqls(druidProperties.getConnectionInitSqls());
            initialParams.append("getConnectionInitSqls : ").append(druidProperties.getConnectionInitSqls()).append(",");
        }
        if (hasValue(druidProperties.getConnProp())) {
            dataSource.setConnectProperties(druidProperties.getConnProp());
            initialParams.append("getConnProp : ").append(druidProperties.getConnProp()).append(",");
        }
        try {
            log.info("dataSource.init() start");
            log.info("dataSource init param is : ", initialParams.toString());
            dataSource.init();
            log.info("dataSource.init() success!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataSource;
    }

    private void valid(DruidProperties druidProperties) {
        if (!hasValue(druidProperties.getUrl()) || !hasValue(druidProperties.getUsername()) || !hasValue(druidProperties.getPassword())) {
            throw new DruidRuntimeException("please check if set url or username or password");
        }
    }

    private boolean hasValue(Integer integer) {
        return integer != null && integer > 0;
    }

    private boolean hasValue(Properties prop) {
        return prop != null && !prop.isEmpty();
    }

    private boolean hasValue(Collection collection) {
        return collection != null && !collection.isEmpty();
    }
    private boolean hasValue(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private boolean hasValue(Boolean bool) {
        return bool != null;
    }
}

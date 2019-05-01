package com.spring.boot.toturial.druid;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:27
 */

@Configuration
@EnableConfigurationProperties(DruidProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.druid.multi-datasource.enabled", havingValue = "false", matchIfMissing = true)
public class DruidAutoConfiguration {

    @Bean
    public DataSource dataSource(DruidProperties druidProperties) {
        return new SingleDataSource().buildDataSource(druidProperties);
    }
}

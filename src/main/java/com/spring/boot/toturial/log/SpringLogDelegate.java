package com.spring.boot.toturial.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Configuration;

/**
 * @author cheny.huang
 * @date 2018-09-10 10:52.
 */
@Slf4j
@Configuration
@ConditionalOnBean(LoggingSystem.class)
public class SpringLogDelegate implements DynamicUpdateSystemLog {
    private final LoggingSystem loggingSystem;
    public SpringLogDelegate(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }

    @Override
    public void setLog(String name, String level) {
        log.info("动态更新日志,name:{},level:{}", name, level);
        try {
            loggingSystem.setLogLevel(name, LogLevel.valueOf(level.toUpperCase()));
        } catch (Exception e) {
            log.error("动态修改日志失败,", e);
        }
    }

    @Override
    public void removeLog(String name) {
        log.info("动态删除日志name:{}", name);
        loggingSystem.setLogLevel(name, null);
    }
}

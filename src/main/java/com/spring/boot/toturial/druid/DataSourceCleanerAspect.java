package com.spring.boot.toturial.druid;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.sql.Connection;

/**
 * <p>清理多数据源</p>
 * @author cheny.huang
 * @date 2019-04-18 15:57.
 */
@Aspect
@Slf4j
public class DataSourceCleanerAspect {
    @Pointcut("execution(* javax.sql.DataSource.getConnection())")
    public void datasourceConnectionPointcut() {
    }

    @Around("datasourceConnectionPointcut()")
    public Object invokeWithDataSource(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Connection connection = (Connection) pjp.proceed();
            if (log.isDebugEnabled()) {
                log.debug("current connection:{}",connection.getMetaData().getURL());
            }
            return connection;
        } finally {
            log.debug("重置到默认的数据源");
            MultipleDataSource.cleanAtOnceCompletion();
        }
    }
}

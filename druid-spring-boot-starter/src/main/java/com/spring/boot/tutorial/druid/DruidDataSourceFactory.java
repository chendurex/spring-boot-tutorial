package com.spring.boot.tutorial.druid;

import javax.sql.DataSource;
import java.util.Set;

/**
 * 暴露多数据源
 * @author cheny.huang
 * @date 2018-12-26 20:56.
 */
public interface DruidDataSourceFactory {
    /**
     * 获取指定数据源
     * @param name 数据源名称
     * @return
     */
    DataSource fetch(String name);

    /**
     * 所有的数据源集合
     * @return
     */
    Set<String> names();
}

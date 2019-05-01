package com.spring.boot.tutorial.log;

/**
 * @author cheny.huang
 * @date 2018-09-10 10:50.
 */
public interface DynamicUpdateSystemLog {
    /**
     * 重新设置日志级别
     * @param name 日志路径,example:com.to8to.sc.eureka.boot
     * @param level 日志级别,example:info
     */
    void setLog(String name, String level);

    /**
     * 移除某个路径下的日志界别
     * @param name 日志路径
     */
    void removeLog(String name);
}

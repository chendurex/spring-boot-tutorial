package com.spring.boot.toturial.druid.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author cheny.huang
 * @date 2018-07-30 17:39.
 */
@SpringBootApplication(scanBasePackages = "com.chen.spring.boot")
@MapperScan(value = "com.chen.spring.boot.druid.mybatis")

public class Application {
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(Application.class).run(args);
    }
}

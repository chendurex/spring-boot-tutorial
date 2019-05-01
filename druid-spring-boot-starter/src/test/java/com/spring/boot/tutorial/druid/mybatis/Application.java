package com.spring.boot.tutorial.druid.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author cheny.huang
 * @date 2018-07-30 17:39.
 */
@SpringBootApplication(scanBasePackages = "com.spring.boot.tutorial")
@MapperScan(value = "com.spring.boot.tutorial.druid.mybatis")

public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }
}

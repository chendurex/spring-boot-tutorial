package com.spring.boot.tutorial;

import org.springframework.boot.builder.SpringApplicationBuilder;
/**
 * @author chendurex
 * @date 2019-05-01 17:00
 */
@SpringBootStarterAnnotation
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }
}


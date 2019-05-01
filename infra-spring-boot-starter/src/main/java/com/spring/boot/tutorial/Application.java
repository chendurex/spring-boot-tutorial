package com.spring.boot.tutorial;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author cheny.huang
 * @date 2018-03-05 16:38.
 */
@SpringBootStarterAnnotation
public class Application {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }
}

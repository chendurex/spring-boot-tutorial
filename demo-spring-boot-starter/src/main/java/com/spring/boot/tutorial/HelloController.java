package com.spring.boot.tutorial;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chendurex
 * @date 2019-05-01 17:05
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public String say() {
        return "hello world";
    }
}

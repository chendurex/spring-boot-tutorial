package com.spring.boot.tutorial.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author cheny.huang
 * @date 2019-01-02 20:04.
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RedisUser implements Serializable {
    private String name;
    private Integer age;
}

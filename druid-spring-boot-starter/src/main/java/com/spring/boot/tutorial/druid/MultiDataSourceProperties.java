package com.spring.boot.tutorial.druid;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cheny.huang
 * @date 2018-11-15 10:47.
 */
@ConfigurationProperties(prefix = "spring.druid")
public class MultiDataSourceProperties {

    private Map<String ,Map<String ,String>> multi  =  new HashMap<>();

    public Map<String, Map<String, String>> getMulti() {
        return multi;
    }

    public void setMulti(Map<String, Map<String, String>> multi) {
        this.multi = multi;
    }
}

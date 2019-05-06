package com.spring.boot.tutorial.rabbitmq.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author cheny.huang
 * @date 2018-07-30 17:14.
 */
@ConfigurationProperties(prefix = "spring.rabbitmq")
class MqConfigProperties {
    private String host;
    private String username;
    private String password;
    private Integer port;
    private String mode;
    private Integer maxAttempts;
    private Integer maxInterval;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
    }
}

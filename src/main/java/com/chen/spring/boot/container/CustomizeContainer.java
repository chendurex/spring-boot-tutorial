package com.chen.spring.boot.container;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cheny.huang
 * @date 2019-03-05 18:33:23.
 */
@ConditionalOnWebApplication
@Configuration
public class CustomizeContainer {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> myWebServerFactoryCustomizer(
            @Value("${spring.tomcat.max.http.header.size:8192}")Integer maxHttpHeaderSize,
            @Value("${spring.tomcat.keepalive.timeout:60000}")Integer keepAliveTimeout,
            @Value("${spring.tomcat.keepalive.requests:-1}")Integer maxKeepAliveRequests,
            @Value("${server.port}")Integer port) {

        return factory -> ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(connector -> {
            Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
            protocolHandler.setMaxHttpHeaderSize(maxHttpHeaderSize);
            protocolHandler.setKeepAliveTimeout(keepAliveTimeout);
            protocolHandler.setMaxKeepAliveRequests(maxKeepAliveRequests);
            protocolHandler.setPort(port);
        }, gracefulShutdown());
    }

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    /**
     * <a href="https://github.com/spring-projects/spring-boot/issues/4657"/a>
     */
    private static class GracefulShutdown implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {
        private static final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);
        private volatile Connector connector;

        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            this.connector.pause();
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            if (executor instanceof ThreadPoolExecutor) {
                try {
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                    log.info("server container ready to stop, current pool size:{}, execute task thread count:{}," +
                                    " total task count:{}, queue size:{}, completed task count:{}",
                            threadPoolExecutor.getPoolSize(),threadPoolExecutor.getActiveCount(),
                            threadPoolExecutor.getTaskCount(), threadPoolExecutor.getQueue(),
                            threadPoolExecutor.getCompletedTaskCount());
                    threadPoolExecutor.shutdown();
                    if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                        log.warn("Tomcat thread pool did not shut down gracefully within "
                                + "30 seconds. Proceeding with forceful shutdown");
                    }
                } catch (InterruptedException ex) {
                    log.warn("", ex);
                    Thread.currentThread().interrupt();
                }
            }
        }

    }
}

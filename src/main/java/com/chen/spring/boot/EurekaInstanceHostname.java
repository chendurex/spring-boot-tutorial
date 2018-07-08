package com.chen.spring.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.net.*;
import java.util.*;

/**
 * eureka在实现集群的时候，是必须通过eureka.instance.hostname来区别本机与其它的服务
 * 如果在配置集群的时候，这样会导致一些不必要的环境配置，影响集群部署
 * 实现思路是将本机地址与所有的集群地址进行比较，如果匹配成功，说明两台机器属于集群环境，然后再将serviceURL中的hostname
 * 作为本机instance.hostname
 * 实现{@link ServletContextInitializer}是目前比较稳妥的方案，因为容器启动后，会加载一系列{@link ServletContextInitializer}
 * 其中就包括eureka中获取instance.hostname的值，如果在{@link ServletContextInitializer}之后则无法生效
 * 当然可以通过{@link ApplicationContextInitializer}实现，但是获取的数据要么需要从配置中心获取，要么需要自己实现获取资源
 * 的方式，因为{@link ApplicationContextInitializer}在项目自动时候注册，此时资源并未加载
 * @author cheny.huang
 * @date 2018-06-16 20:32.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EurekaInstanceHostname implements ServletContextInitializer {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_EUREKA_CLIENT_SERVICE_URL_ZONE = "eureka.client.serviceUrl.defaultZone";
    private static final String DEFAULT_INSTANCE_NAME = "custom.instance.hostname";
    private volatile boolean suc;
    public EurekaInstanceHostname(ConfigurableEnvironment env) {
        log.info("开始将eureka的instance.hostname注入到property中");
        init(env);
        suc = true;
        log.info("已经将eureka的instance.hostname注入到property中，当前eureka.instance.hostname:[{}]",
               env.getProperty(DEFAULT_INSTANCE_NAME));
    }
    @Override
    public void onStartup(ServletContext servletContext) {
        if (!suc) {
            throw new IllegalStateException("未将eureka.instance.hostname注入到properties中");
        }
    }

    private void init(ConfigurableEnvironment env) {
        CompositePropertySource source = new CompositePropertySource(DEFAULT_INSTANCE_NAME);
        source.addPropertySource(new EnumerablePropertySource<String>(DEFAULT_INSTANCE_NAME) {
            @Override
            public Object getProperty(String name) {
                return DEFAULT_INSTANCE_NAME.equals(name) ? getDefaultInstanceName(env) : null;
            }

            @Override
            public String[] getPropertyNames() {
                return new String[]{DEFAULT_INSTANCE_NAME};
            }
        });
        env.getPropertySources().addFirst(source);
    }

    private String getDefaultInstanceName(ConfigurableEnvironment configurableEnv) {
        String serviceUrl = configurableEnv.getProperty(DEFAULT_EUREKA_CLIENT_SERVICE_URL_ZONE);
        Collection<String> local = localAddress();
        log.info("all local address is :{}, register url : {}", local, serviceUrl);
        for (String s : serviceUrl.split(",")) {
            final String hostname = hostFromUrl(s);
            final String remoteIp = hostnameToIp(hostname);
            log.info("parser hostname is :{} and translated ip is : {}", hostname, remoteIp);
            if (local.contains(remoteIp)) {
                return hostname;
            }
        }
        throw new IllegalStateException("当前机器还未加入到eureka集群中，请检查配置是否正确");
    }

    private String hostnameToIp(String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            log.error("", e);
            throw new IllegalStateException("获取远程地址失败，请检查是否有配置DNS", e);
        }
    }

    /**
     * 获取本机所有的网络IP，包括本地ip127.0.0.1或者局域网ip192.168.3.255
     */
    private Collection<String> localAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            Set<String> localAddr = new HashSet<>();
            while(enumeration.hasMoreElements()) {
                Enumeration<InetAddress> address = enumeration.nextElement().getInetAddresses();
                while (address.hasMoreElements()) {
                    localAddr.add(address.nextElement().getHostAddress());
                }
            }
            return Collections.unmodifiableCollection(localAddr);
        } catch (Exception e) {
            log.error("", e);
            throw new IllegalStateException("获取本地地址失败", e);
        }
    }

    private String hostFromUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return uri.getHost();
    }
}

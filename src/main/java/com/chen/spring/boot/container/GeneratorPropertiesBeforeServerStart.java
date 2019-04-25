package com.chen.spring.boot.container;

import com.chen.spring.boot.util.IpAddressUtils;
import com.chen.spring.boot.util.RandomPortUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 在容器初始化之前注入各种属性
 * @author cheny.huang
 * @date 2018-09-05 18:33:23.
 */
public class GeneratorPropertiesBeforeServerStart implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_SERVER_PORT = "server.port";
    private static final String APOLLO_BOOTSTRAP_ENABLED = "apollo.bootstrap.enabled";
    private static final String APOLLO_BOOTSTRAP_NAMESPACES = "apollo.bootstrap.namespaces";
    private static final String DEFAULT_APOLLO_BOOTSTRAP_ENABLED_VALUE = "true";
    private static final String DEFAULT_APOLLO_BOOTSTRAP_NAMESPACES_VALUE = "application, private, 1.t8t-sc-public-default";
    private static final String DEFAULT_LOCAL_ENV = "server.local.env";
    private static final String SC_INFRASTRUCTURE_VERSION = "sc.infrastructure.version";
    private static final String DEFAULT_EUREKA_INSTANCE_ID = "eureka.instance.instance-id";
    private static final String DEFAULT_EUREKA_INSTANCE_HOST_NAME = "eureka.instance.hostname";
    private PropertySource<?> localPropertySource;
    private ConfigurableEnvironment env;
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        env = applicationContext.getEnvironment();
        if (env.containsProperty(DEFAULT_LOCAL_ENV)) {
            return ;
        }
        // 先解析本地存在的xml文件，下面获取属性时会用到
        parserApplicationNameAndPortFromYml();
        // 因为这个时候日志并未生效，所以只能用println方式打印
        System.out.println("开始注入[server.port,eureka.instance.instance-id,eureka.instance.hostname," +
                "apollo.bootstrap.enabled,apollo.bootstrap.namespaces]注入到property中");
        init();
        System.out.println(String.format("成功将[server.port,eureka.instance.instance-id,eureka.instance.hostname," +
                        "apollo.bootstrap.enabled,apollo.bootstrap.namespaces]注入到property中，" +
                        "[%s],[%s],[%s],[%s],[%s]",
                getValueFromYml(DEFAULT_SERVER_PORT), getValueFromYml(DEFAULT_EUREKA_INSTANCE_ID), getValueFromYml(DEFAULT_EUREKA_INSTANCE_HOST_NAME),
                DEFAULT_APOLLO_BOOTSTRAP_ENABLED_VALUE, DEFAULT_APOLLO_BOOTSTRAP_NAMESPACES_VALUE));
    }

    private void init() {
        generatorServerPortIfNotExist();
        setApolloProperties();
        setLocalEnv();
        setEurekaInstanceId();
        setScInfrastructureVersion();
    }

    /**
     * apollo通过apollo.bootstrap.enabled使配置在容器启动前注入
     * 但是此配置生效的前提是需要yml文件中配置或者在启动时设置参数
     * 如果yml没有解析之前，是无法获取到这个配置，所以希望在yml解析之前就获取到此配置，达到真正的快速注入属性
     * 比如在配合spring自定义动态日志时候，因为日志是在容器启动时候进行解析，而此时apollo的配置文件还未加载，
     * 导致无法获取到apollo的配置，也就无法实现根据配置实现动态的加载日志
     */
    private void setApolloProperties() {
        CompositePropertySource apolloBootstrapEnabledSource = new CompositePropertySource(APOLLO_BOOTSTRAP_ENABLED);
        apolloBootstrapEnabledSource.addPropertySource(new EnumerablePropertySource<String>(APOLLO_BOOTSTRAP_ENABLED) {
            @Override
            public String getProperty(String name) {
                return APOLLO_BOOTSTRAP_ENABLED.equals(name) ? DEFAULT_APOLLO_BOOTSTRAP_ENABLED_VALUE : null;
            }

            @Override
            public String[] getPropertyNames() {
                return new String[]{APOLLO_BOOTSTRAP_ENABLED};
            }
        });
        env.getPropertySources().addLast(apolloBootstrapEnabledSource);

        CompositePropertySource apolloBootstrapNamespacesSource = new CompositePropertySource(APOLLO_BOOTSTRAP_NAMESPACES);
        apolloBootstrapNamespacesSource.addPropertySource(new EnumerablePropertySource<String>(APOLLO_BOOTSTRAP_NAMESPACES) {
            @Override
            public String getProperty(String name) {
                return APOLLO_BOOTSTRAP_NAMESPACES.equals(name) ? DEFAULT_APOLLO_BOOTSTRAP_NAMESPACES_VALUE : null;
            }
            @Override
            public String[] getPropertyNames() {
                return new String[]{APOLLO_BOOTSTRAP_NAMESPACES};
            }
        });
        env.getPropertySources().addLast(apolloBootstrapNamespacesSource);
    }

    /**
     * 根据不同环境，默认将日志打印到不同的控制台
     */
    private void setLocalEnv() {
        CompositePropertySource apolloBootstrapNamespacesSource = new CompositePropertySource(DEFAULT_LOCAL_ENV);
        apolloBootstrapNamespacesSource.addPropertySource(new EnumerablePropertySource<String>(DEFAULT_LOCAL_ENV) {
            @Override
            public String getProperty(String name) {
                return DEFAULT_LOCAL_ENV.equals(name) ? Boolean.toString(System.getProperty("os.name").startsWith("Windows")) : null;
            }
            @Override
            public String[] getPropertyNames() {
                return new String[]{DEFAULT_LOCAL_ENV};
            }
        });
        env.getPropertySources().addLast(apolloBootstrapNamespacesSource);
    }

    /**
     * 设置框架的版本号
     */
    private void setScInfrastructureVersion() {
        CompositePropertySource infrastructureVersion = new CompositePropertySource(SC_INFRASTRUCTURE_VERSION);
        infrastructureVersion.addPropertySource(new EnumerablePropertySource<String>(SC_INFRASTRUCTURE_VERSION) {
            @Override
            public String getProperty(String name) {
                return SC_INFRASTRUCTURE_VERSION.equals(name) ? getClass().getPackage().getImplementationVersion() : null;
            }
            @Override
            public String[] getPropertyNames() {
                return new String[]{SC_INFRASTRUCTURE_VERSION};
            }
        });
        env.getPropertySources().addLast(infrastructureVersion);
    }

    /**
     * 如果xml中或者apollo中并未配置端口，则生成一个随机端口
     */
    private void generatorServerPortIfNotExist() {
        String v = getValueFromYml(DEFAULT_SERVER_PORT);
        if (!StringUtils.isEmpty(v)) {
            return ;
        }
        String backup = RandomPortUtils.getAvailablePort();
        log.info("设置一个备用的随机server.port：[{}]", backup);
        System.out.println("设置一个备用的随机server.port：["+backup+"]");
        CompositePropertySource serverPortSource = new CompositePropertySource(DEFAULT_SERVER_PORT);
        serverPortSource.addPropertySource(new EnumerablePropertySource<String>(DEFAULT_SERVER_PORT) {
            @Override
            public String getProperty(String name) {
                return DEFAULT_SERVER_PORT.equals(name) ? backup : null;
            }

            @Override
            public String[] getPropertyNames() {
                return new String[]{DEFAULT_SERVER_PORT};
            }
        });
        // 如果配置有设置以配置为准，否则以它为准
        env.getPropertySources().addLast(serverPortSource);
    }

    /**
     * 将eureka界面以IP:PORT形式显示出来
     */
    private void setEurekaInstanceId() {
        String ip = IpAddressUtils.getExternalIp();
        String appName = getValueFromYml("spring.application.name");
        if (StringUtils.isEmpty(appName)) {
            throw new RuntimeException("请设置spring.application.name");
        }
        CompositePropertySource eurekaInstanceId = new CompositePropertySource(DEFAULT_EUREKA_INSTANCE_ID);
        eurekaInstanceId.addPropertySource(new EnumerablePropertySource<String>(DEFAULT_EUREKA_INSTANCE_ID) {
            @Override
            public String getProperty(String name) {
                return DEFAULT_EUREKA_INSTANCE_ID.equals(name) ? ip+":"+appName+":"+getValueFromYml("server.port"): null;
            }
            @Override
            public String[] getPropertyNames() {
                return new String[]{DEFAULT_EUREKA_INSTANCE_ID};
            }
        });
        CompositePropertySource eurekaHostname = new CompositePropertySource(DEFAULT_EUREKA_INSTANCE_HOST_NAME);
        eurekaHostname.addPropertySource(new EnumerablePropertySource<String>(DEFAULT_EUREKA_INSTANCE_HOST_NAME) {
            @Override
            public String getProperty(String name) {
                return DEFAULT_EUREKA_INSTANCE_HOST_NAME.equals(name) ? ip: null;
            }
            @Override
            public String[] getPropertyNames() {
                return new String[]{DEFAULT_EUREKA_INSTANCE_HOST_NAME};
            }
        });
        env.getPropertySources().addLast(eurekaInstanceId);
        env.getPropertySources().addLast(eurekaHostname);
    }

    private void parserApplicationNameAndPortFromYml() {
        ClassPathResource classPathResource = new ClassPathResource("application.yml");
        if (!classPathResource.exists()) {
            throw new RuntimeException("请先创建[application.yml]文件,并设置好spring.application.name再启动项目");
        }
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        try {
            localPropertySource = loader.load("applicationConfig: [classpath:/application.yml]", classPathResource).get(0);
        } catch (IOException e) {
            throw new RuntimeException("加载application.yml文件失败", e);
        }
    }

    private String getValueFromYml(String key) {
        if (localPropertySource != null) {
            Object v = localPropertySource.getProperty(key);
            return v == null ? env.getProperty(key) : v.toString();
        }
        return env.getProperty(key);
    }
}

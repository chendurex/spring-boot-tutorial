package com.spring.boot.tutorial.log;

import com.spring.boot.tutorial.container.GeneratorPropertiesBeforeServerStart;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>动态更新日志(因为项目是采用jar包启动的，无法使用logback提供的动态更新配置功能)
 * 因为spring集成的日志是在容器启动之前解析，所以不会从{@link org.springframework.context.annotation.PropertySource}
 * 获取属性，也就导致了无法将日志配置放入到配置中心
 * 现在通过定义一个基础的日志配置(包括基本的日志路径，日志打印等功能)，spring初始化的时候加载基本的日志配置文件
 * 当加载完基础配置后，再获取配置中心配置的个性化日志文件，重新追加到日志配置中，最后有新的配置发生变化，则直接
 * 再次更新配置，以达到动态日志效果
 * </p>
 * <p>
 *  @update 2019-03-15
 *  @description 移除非法的日志配置
 *  因为{@link GeneratorPropertiesBeforeServerStart#setApolloProperties()}已经解决了在容器启动的时候注入日志配置文件，
 *  所以可以直接在apollo配置各种动态日志，但是这里却导致了一些客户端随意或者无意配置一些不安全的日志(如logging.level.com)
 *  导致线上大量打印无用的日志，现在需要从新在{@link InitializingBean#afterPropertiesSet()}控制这些配置，如果有存在则移除
 * </p>
 * @author cheny.huang
 * @date 2018-09-10 11:21.
 */
@Configuration
@Import(SpringLogDelegate.class)
@Slf4j
public class DynamicLogListener implements InitializingBean {
    private static final String PREFIX = "logging.level";
    private static final String ROOT_LOG_LEVEL = "logging.level.com.to8to";
    private final DynamicUpdateSystemLog systemLog;
    public DynamicLogListener(DynamicUpdateSystemLog systemLog) {
        this.systemLog = systemLog;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<String> pros = ConfigService.getAppConfig().getPropertyNames();
        List<String> customizeLogConfig = filterUntrustedLogConfig(pros);
        customizeLogConfig.forEach(s->{log.error("移除非法的日志配置:{}", s);remove(s);});
    }

    private List<String> filterUntrustedLogConfig(Set<String> pros) {
        return pros.stream()
                .filter(s -> s.startsWith(PREFIX))
                .filter(s -> s.equals(ROOT_LOG_LEVEL) || !s.contains(ROOT_LOG_LEVEL))
                .collect(Collectors.toList());
    }

    private List<String> filterLogConfig(Set<String> pros) {
        return pros.stream()
                .filter(s -> s.startsWith(PREFIX))
                .filter(s -> s.contains(ROOT_LOG_LEVEL))
                .filter(s -> !s.equals(ROOT_LOG_LEVEL))
                .collect(Collectors.toList());
    }

    private void changeLog(String key, String value) {
        if (value == null) {
            remove(key);
        } else {
            update(key, value);
        }
    }
    private void update(String key, String value) {
        systemLog.setLog(key.substring(PREFIX.length()+1), value);
    }
    private void remove(String key) {
        systemLog.removeLog(key.substring(PREFIX.length()+1));
    }

    @ApolloConfigChangeListener
    private void changeLog(ConfigChangeEvent changeEvent) {
        List<String> changes = filterLogConfig(changeEvent.changedKeys());
        changes.forEach(key-> {
            String oldValue = changeEvent.getChange(key).getOldValue();
            String newValue = changeEvent.getChange(key).getNewValue();
            log.info("修改日志配置，key:{},oldValue:{}, newValue:{}", key, oldValue, newValue);
            changeLog(key, newValue);
            }
        );
    }
}

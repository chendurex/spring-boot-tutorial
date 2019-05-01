package com.spring.boot.tutorial.serialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * <p>还原jackson修改的字段名称</p>
 * <p>业务定义的字段属于非标准的，而jackson会将非标准字段转换为标准字段(如定义字段为aB、a_、Name这种非java标准的驼峰式名称)
 * 虽然可以通过{@link JsonProperty}注解处理，但是jackson实际上会将标准的和非标准的两个
 * 字段值都返回给前端，如果前端不做处理再次将数据返回给后端，会导致最终以标准的字段覆盖非标准的字段，所以在返回给前端的
 * 时候就将jackson解析的字段都移除掉(比如存在Value和value两个key，应该更新Value字段，而实际更新value字段，导致value字段把
 * Value字段的值给修改了)
 * </p>
 * @author cheny.huang
 * @date 2019-01-09 17:51.
 */
@Slf4j
public class CustomizePropertyNamingStrategy {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.propertyNamingStrategy(new CustomizeStrategy());
    }

    class CustomizeStrategy extends PropertyNamingStrategy {

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            return valid(defaultName, method.getName()) ? defaultName : originFieldName(method.getDeclaringClass(), defaultName);
        }
    }

    private String originFieldName(Class<?> clz, String name) {
        String originName = null;
        Set<String> repeat = new HashSet<>();
        for (Field field : clz.getDeclaredFields()) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null) {
                if (!repeat.add(jsonProperty.value())) {
                    // 抛出异常也是写警告日志，不会影响到启动流程
                    log.error("重复定义属性名["+jsonProperty.value()+"]，请尽快修改，否则引用当前类的controller不可用");
                }
            }
            if (field.getName().equalsIgnoreCase(name)) {
                if (originName != null) {
                    log.error("同时存在多个同名但是大小写不一致的名称,第一个为：{}，第二个为：{}", originName, field.getName());
                }
                if (jsonProperty != null) {
                    originName = jsonProperty.value();
                } else {
                    originName = field.getName();
                }
                log.info("jackson自动生成了标准的字段别名，现在还原成标准的名称,定义的名称是：{}，而jackson名称为：{}", originName, name);
            }
        }
        return Optional.ofNullable(originName).orElse(name);
    }

    private boolean valid(String name, String methodName) {
        if (name.startsWith("_")) {
            log.error("字段不能以下划线开头,{}", name);
        }
        if (!manipulateGetMethod(name).equals(methodName)) {
            log.warn("移除jackson生成的冗余字段,原始字段：{}", name);
            return false;
        }
        return true;
    }

    private String manipulateGetMethod(String field) {
        return "get"+Character.toUpperCase(field.charAt(0))+field.substring(1);
    }
}

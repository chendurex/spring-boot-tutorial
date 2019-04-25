package com.chen.spring.boot.swagger;

import com.chen.spring.boot.util.IpAddressUtils;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.property.BeanPropertyNamingStrategy;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author cheny.huang
 * @date 2019-03-23 15:04.
 */
@ConditionalOnProperty(value = "spring.swagger2.enabled", havingValue = "true")
@EnableSwagger2
@Configuration
@Slf4j
public class SwaggerConfiguration {

    /**
     * swagger2按照标准的javabean规范解析字段，导致非标准驼峰式的命名会转换成标准的驼峰式命名
     * 可以通过jackson的{@link com.fasterxml.jackson.annotation.JsonProperty}字段绑定正确的名称
     * 但是swagger2在解析时，还会再次判断方法中的get方法，导致会再次生成一个字段，导致不标准的名称
     * 生成两个名字，所以重新处理名称
     * 比如eName会生成eName和ename两个字段，我们需要移除ename这个字段
     * @author cheny.huang
     * @date 2019-02-21 18:31.
     * @see springfox.documentation.schema.property.ObjectMapperBeanPropertyNamingStrategy
     * @see com.chen.spring.boot.serialize.CustomizePropertyNamingStrategy
     */
    @Primary
    @Bean
    BeanPropertyNamingStrategy swagger2JacksonPropertyNamingStrategy() {
        return new BeanPropertyNamingStrategy() {
            @Override
            public String nameForSerialization(BeanPropertyDefinition beanProperty) {
                // 如果是方法生成的字段，直接返回原始标准的字段，后续swagger会做去重操作，移除冗余字段
                if (beanProperty.getField() == null) {
                    String v = beanProperty.getGetter().getName().substring(3);
                    log.info("移除jackson自动生成的名称:{}", beanProperty.getName());
                    return v;
                }
                return beanProperty.getName();
            }

            @Override
            public String nameForDeserialization(BeanPropertyDefinition beanProperty) {
                if (beanProperty.getField() == null) {
                    String v = beanProperty.getGetter().getName().substring(3);
                    // 如果有使用@JsonProperty定义的名称，并且名称并不是简单的大小写形式，那么还是存在产生无用的字段
                    // 如果要解决，可以采用com.to8to.sc.spring.CustomizePropertyNamingStrategy方法
                    // 目前可以不用处理，因为这个仅仅是文档工具而已，影响不会很大
                    log.info("移除jackson自动生成的名称:{}", beanProperty.getName());
                    return v;
                }
                return beanProperty.getName();
            }
        };
    }

    @Bean
    Docket productApi(@Value("${server.port}")String host,
                      @Value("${spring.swagger2.customized.header:}")String customizedHeader) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .build()
                .host(IpAddressUtils.getExternalIp() + ":" + host)
                // 将byte字段改为int
                .directModelSubstitute(Byte.class, int.class)
                .directModelSubstitute(byte.class, int.class)
                .globalOperationParameters(customizeHeader(customizedHeader))
                .globalResponseMessage(RequestMethod.GET, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.HEAD, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.OPTIONS, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.PATCH, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.PUT, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.DELETE, defaultResponseMessage())
                .globalResponseMessage(RequestMethod.POST, defaultResponseMessage());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("项目文档描述").build();
    }

    private List<ResponseMessage> defaultResponseMessage() {
        return Arrays.asList(new ResponseMessageBuilder().code(400).message("缺少请求参数").build()
                , new ResponseMessageBuilder().code(401).message("未授权的客户端").build()
                , new ResponseMessageBuilder().code(403).message("没有权限访问当前地址").build()
                , new ResponseMessageBuilder().code(404).message("请求路径不对，找不到资源").build()
                , new ResponseMessageBuilder().code(500).message("服务器内部出错").build());
    }

    private List<Parameter> customizeHeader(String customizeHeader) {
        if (customizeHeader == null || customizeHeader.isEmpty()) {
            return Collections.emptyList();
        }
        //String headerModel = "headerName1,type,defaultValue1|headerName2,type,defaultValue2|etc...";
        List<Parameter> parameter = new ArrayList<>();
        for (String parameters : customizeHeader.split("\\|")) {
            String[]s = parameters.split(",");
            parameter.add(new ParameterBuilder().parameterType("header").name(s[0]).description(s[0])
                    .defaultValue(s.length>2 && (!s[2].isEmpty()) ? s[2] : null)
                    .modelRef(new ModelRef(s.length>1&&(!s[1].isEmpty()) ? s[1] : "String")).build());
        }
        return parameter;
    }
}

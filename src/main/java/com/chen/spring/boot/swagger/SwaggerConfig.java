package com.chen.spring.boot.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

/**
 * @author chen
 *         2017/11/1 10:02
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .build()
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
}
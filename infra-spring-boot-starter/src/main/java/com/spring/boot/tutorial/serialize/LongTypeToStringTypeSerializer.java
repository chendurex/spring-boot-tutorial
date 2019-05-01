package com.spring.boot.tutorial.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * javascript 精度问题导致后端传过来的值显示不正确
 * ----------
 * JS中,整形的最大的值为 Number.MAX_SAFE_INTEGER
 * 2^{53}  - 1=9007199254740991
 * ----------
 * 
 * spring 4.1.9 以上有效
 * 重写springmvc json序列化，把Long类型转换成字符串，再传到前端
 * springmvc配置如下
 * <mvc:annotation-driven>
 * 	<mvc:message-converters register-defaults="false">
 * 		<bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
 * 			<property name="objectMapper">
 * 				<bean class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean">
 * 					<property name="serializers">
 * 						<array>
 * 							<bean class="com.spring.boot.tutorial.serialize.LongTypeToStringTypeSerializer"/>
 * 						</array>
 * 					</property>
 * 				</bean>
 *			</property> 
 * 		</bean>
 * 	</mvc:message-converters>
 * </mvc:annotation-driven>
 * 
 * @author chen
 * @date 2016/7/6 16:36
 */
public class LongTypeToStringTypeSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        jgen.writeString(String.valueOf(value));
    }

    @Override
    public Class<Long> handledType() {
        return Long.class;
    }
}

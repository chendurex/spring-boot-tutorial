package com.spring.boot.tutorial.compile;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * 由于jackson序列化时，要求变量名必须是标准的，否则导致无法注入属性内容，
 * 所以在编译的时候检查代码是否符合规范，如果不符合规范则提示对方修改成规范的
 * @author cheny.huang
 * @date 2018-12-12 15:10.
 */
@SupportedAnnotationTypes(value = "lombok.Data")
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class VariableValidProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "检测当前项目是否存在不合法的变量名...");
        for (Element element : roundEnv.getElementsAnnotatedWith(Data.class)) {
            for (VariableElement field : ElementFilter.fieldsIn(element.getEnclosedElements())) {
                final String name = field.getSimpleName().toString();
                String convert = legacyManglePropertyName(manipulateGetMethod(name), 3);
                if (!convert.equals(name)) {
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    if (jsonProperty == null || convert.equals(jsonProperty.value())) {
                        String error = String.format("变量名[%s]属于非标准变量名，请改为[%s],或者更改为标准的驼峰式变量名" +
                                "，如果已经在使用中，那么通过在变量名前增加[@JsonProperty(\"%s\")]别名", name, convert, name);
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, field);
                    }
                }
            }
        }
        return true;
    }

    private String manipulateGetMethod(String field) {
        return "get"+Character.toUpperCase(field.charAt(0))+field.substring(1);
    }
    /**
     * 返回jackson标准的变量名
     * @param basename 方法名
     * @param offset 偏移值
     * @return
     */
    private String legacyManglePropertyName(final String basename, final int offset)
    {
        final int end = basename.length();
        if (end == offset) {
            return null;
        }
        // next check: is the first character upper case? If not, return as is
        char c = basename.charAt(offset);
        char d = Character.toLowerCase(c);

        if (c == d) {
            return basename.substring(offset);
        }
        // otherwise, lower case initial chars. Common case first, just one char
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(d);
        int i = offset+1;
        for (; i < end; ++i) {
            c = basename.charAt(i);
            d = Character.toLowerCase(c);
            if (c == d) {
                sb.append(basename, i, end);
                break;
            }
            sb.append(d);
        }
        return sb.toString();
    }
}

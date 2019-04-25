package com.chen.spring.boot.compile;

import com.chen.spring.boot.SpringBootStarterAnnotation;
import com.chen.spring.boot.response.ResResult;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * 编译时期检查代码是否符合规范
 * @author cheny.huang
 * @date 2018-10-16 10:32.
 */
@SupportedAnnotationTypes(
        value = {"com.chen.spring.boot.SpringBootStarterAnnotation",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.DeleteMapping",
                "org.springframework.stereotype.Service"
        })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DevSpecProcessor extends AbstractProcessor {
    private static final String PACKAGE_START = "com.chen.spring.boot";
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "检测当前项目返回值是否符合标准...");
        for (Element element : roundEnv.getElementsAnnotatedWith(SpringBootStarterAnnotation.class)) {
            processPackageValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Service.class)) {
            processServiceValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RequestMapping.class)) {
            processRequestValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(PutMapping.class)) {
            processMappingValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(PostMapping.class)) {
            processMappingValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(DeleteMapping.class)) {
            processMappingValidated(element);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(GetMapping.class)) {
            processMappingValidated(element);
        }
        return true;
    }

    private void processPackageValidated(Element element) {
        String path = element.toString().substring(0, element.toString().lastIndexOf("."));
        if (!path.startsWith(PACKAGE_START)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "包路径必须以【"+PACKAGE_START+"】开头"+",当前包路径为["+path+"]", element);
        }
    }

    private void processRequestValidated(Element element) {
        if (!element.getKind().isClass()) {
            processMappingValidated(element);
        }
    }

    private void processMappingValidated(Element element) {
        ExecutableElement executableElement = ((ExecutableElement)element);
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(ResResult.class.getCanonicalName());
        boolean isResType = processingEnv.getTypeUtils().isAssignable(executableElement.getReturnType(), typeElement.asType());
        if (!isResType) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "对外暴露的接口【"+element+"】返回值必须是["+ResResult.class.getName()+"]类型", element);
        }
    }


    private void processServiceValidated(Element element) {
        TypeElement typeElement = (TypeElement)element;
        //Service service = element.getAnnotation(Service.class);
        if (typeElement.getInterfaces().size() == 0) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@Service类【"+element+"】必须以接口的方法暴露API", element);
        }
    }
}

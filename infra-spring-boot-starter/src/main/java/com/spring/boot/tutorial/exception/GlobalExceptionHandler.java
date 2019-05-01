package com.spring.boot.tutorial.exception;

import com.spring.boot.tutorial.response.ResResult;
import com.spring.boot.tutorial.response.ResUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.stream.Collectors;

/**
 * 异常包括请求路径异常，请求参数类型不匹配异常，请求方法类型异常、请求头格式不匹配异常、参数验证异常、全局异常
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 * @author chendurex
 * @date 2018-07-01 20:48
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {


    /**
     * 请求参数验证失败时，比如RequestBody中使用了@Validated(alias @Valid)，返回此异常
     * 注意，spring 仅支持RequestBody参数校验，不支持RequestParam参数校验，如果需要支持RequestParam则需要自己自定义实现了
     *
     * @see RequestResponseBodyMethodProcessor#resolveArgument(MethodParameter, ModelAndViewContainer, NativeWebRequest, WebDataBinderFactory)
     * @see RequestParamMethodArgumentResolver#resolveArgument(MethodParameter, ModelAndViewContainer, NativeWebRequest, WebDataBinderFactory)
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResResult handle(MethodArgumentNotValidException exception) {
        return error(exception.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList()));
    }

    /**
     * 如果未找到requestMapping对应的解析方法则会抛出此异常
     * 需要设置 {@link DispatcherServlet#setThrowExceptionIfNoHandlerFound(boolean)} true
     * 注意：boot默认开启了访问资源文件，在未找到对应的mapping映射时候会再次访问静态资源，如果再次找不到则会返回error错误，
     * 由{@link org.springframework.boot.web.servlet.error.ErrorController} 进行处理，所以要抛出此异常还得禁止静态资源访问
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResResult handle(NoHandlerFoundException exception) {
        return error(exception.getRequestURL());
    }

    /**
     * RequestParam缺少必填项
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResResult handle(MissingServletRequestParameterException exception) {
        String msg = "缺少参数["+exception.getParameterName()+":"+exception.getParameterType()+"]";
        return error(msg);
    }

    /**
     * 适配{@link RequestMapping#params()}中的参数，比如只有某个请求值必须存在时才匹配或者使用模板条件映射的时候会需要用到。
     *   如定义一个总的RequestMapping映射，然后具体的请求通过请求参数中action来匹配
     *   <pre>
     *     <code>@GetMapping(value = "/a", params = {"action=insert"})</code>
     *     public void a(@RequestParam(value = "a", required = false) Integer a, @RequestParam("b") String b) {
     *         System.out.println("insert enter"+a);
     *     }
     *
     *     <code>@GetMapping(value = "/a", params = {"action=update"})</code>
     *     public void b(@RequestParam(value = "a", required = false) Integer a, @RequestParam("b") String b) {
     *         System.out.println("update enter"+a);
     *     }
     *   </pre>
     * <a href="http://localhost:8080/resume/a?action=insert&a=1&b=22">invoke insert</a>
     * <a href="http://localhost:8080/resume/a?action=update&a=1&b=22">invoke update</a>
     * @see RequestMapping#params()
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResResult handle(UnsatisfiedServletRequestParameterException exception) {
        return error(exception.getMessage());
    }

    /**
     * 匹配MediaType类型异常，比如我需要是的{@link org.springframework.http.MediaType#APPLICATION_JSON_UTF8_VALUE}
     * 而实际给我的的是{@link org.springframework.http.MediaType#TEXT_PLAIN_VALUE}
     * <pre>
     *     <code>@GetMapping(value = "/a",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)</code>
     *     public void a(@RequestParam(value = "a", required = false) Integer a, @RequestParam("b") String b) {
     *     }
     * </pre>
     * 如果请求头<code>Content-Type:application/json;charset=UTF-8</code>则匹配成功，否则匹配失败
     * @see RequestMapping#consumes()
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResResult handle(HttpMediaTypeException exception) {
        return error(exception.getMessage());
    }

    /**
     * 返回的数据类型与客户端需要的不匹配
     * <pre>
     *     <code>@GetMapping(value = "/a",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)</code>
     *     public void a(@RequestParam(value = "a", required = false) Integer a, @RequestParam("b") String b) {
     *     }
     * </pre>
     * 如果请求头<code>Accept:application/json;charset=UTF-8</code>则匹配成功，否则匹配失败
     * @see #handle(HttpMediaTypeException)
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResResult handle(HttpMediaTypeNotAcceptableException exception) {
        return error(exception.getMessage());
    }

    /**
     * 请求方法类型不匹配则抛出此异常，比如需要的是get方法，而请求给出的是post方法
     * @see #handle(HttpMediaTypeNotSupportedException)
     * @see #handle(HttpMediaTypeNotAcceptableException)
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResResult handle(HttpMediaTypeNotSupportedException exception) {
        return error(exception.getMessage());
    }


    /**
     * 所有的异常控制
     * 保证请求一定能返回规定的数据，而不是404或者500这种错误
     * @param exception
     * @return
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResResult handle(Exception exception) {
        return error(exception.getMessage());
    }

    private ResResult error(Object message) {
        return ResUtils.fail(message);
    }
}

package com.spring.boot.toturial.exception;

import com.chen.spring.boot.response.ResResult;
import com.chen.spring.boot.response.ResUtils;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @see org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
 * @author chendurex
 * @date 2018-07-08 12:56
 */
@RestController
@RequestMapping("${server.error.path:${error.path:/error}}")
public class PageNotFoundHandler extends AbstractErrorController {

    public PageNotFoundHandler(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @Override
    public String getErrorPath() {
        return "nothing to do";
    }

    @RequestMapping
    public ResResult handler(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, true);
        return ResUtils.fail(body);
    }
}

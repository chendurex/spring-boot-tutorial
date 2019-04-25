package com.chen.spring.boot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author cheny.huang
 * @date 2018-09-04 15:33.
 */
@Slf4j
public class BeanUtils {
    private static final ObjectMapper OM = new ObjectMapper();
    private static final ObjectWriter OW = OM.writer().withDefaultPrettyPrinter();

    public static String deepPrint(Object o) {
        try {
            return OW.writeValueAsString(o);
        } catch (Exception e) {
            log.error("格式化数据失败，", e);
            return null;
        }
    }

    public static <T>T parser(byte[] in, Class<T> clz) {
        try {
            return OM.readValue(in, clz);
        } catch (IOException e) {
            log.error("格式化数据失败，", e);
            throw new RuntimeException(e);
        }
    }

}

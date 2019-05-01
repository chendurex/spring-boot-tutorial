package com.spring.boot.toturial.response;

/**
 * @author chendurex
 * @description
 * @date 2018-03-18 21:02
 */
public final class ResUtils {
    private static final String SUC_DESC = Boolean.TRUE.toString();
    private static final String FAIL_DESC = Boolean.FALSE.toString();
    public static ResResult suc(String data) {
        return result(ResStat.SUC, SUC_DESC, data);
    }

    public static <T>ResResult data(T data) {
        return result(ResStat.SUC, SUC_DESC, data);
    }

    public static ResResult suc() {
        return result(ResStat.SUC, SUC_DESC, null);
    }

    public static ResResult fail(String desc) {
        return result(ResStat.FAIL, FAIL_DESC, desc);
    }

    public static ResResult fail(Object desc) {
        return result(ResStat.FAIL, FAIL_DESC, desc);
    }

    private static <T> ResResult result(int code, String desc, T data) {
        return new ResResult<>(code, desc, data);
    }
}

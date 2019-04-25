package com.chen.spring.boot.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author cheny.huang
 * @date 2018-12-19 21:01.
 */
@Slf4j
public class RandomPortUtils {
    private static final String JAVA_SERVER_BIND_PORT_RANGE = "JAVA_SERVER_BIND_PORT_RANGE";
    private static final int DEFAULT_MIN_PORT = 40001;
    private static final int DEFAULT_MAX_PORT = 40999;
    private static final int DEFAULT_PORT_MAX_RETRABLE = 100;
    private static final Set<Integer> USED = new HashSet<>();
    public synchronized static String getAvailablePort() {
        int backup;
        int minPort = DEFAULT_MIN_PORT;
        int maxPort = DEFAULT_MAX_PORT;
        try {
            String portRange = System.getenv(JAVA_SERVER_BIND_PORT_RANGE);
            if (portRange != null) {
                minPort = Integer.valueOf(portRange.split("-")[0].trim());
                maxPort = Integer.valueOf(portRange.split("-")[1].trim());
            }
        } catch (Exception e) {
            log.warn("解析{}范围端口失败，", JAVA_SERVER_BIND_PORT_RANGE, e);
        }

        for (int i=0;i<DEFAULT_PORT_MAX_RETRABLE;i++) {
            backup = ThreadLocalRandom.current().nextInt(minPort, maxPort);
            if (USED.contains(backup)) {
                continue;
            }
            if (available(backup)) {
                USED.add(backup);
                return String.valueOf(backup);
            }
        }
        String err = "无法获取指定的端口";
        log.info("{}", err);
        System.out.println(err);
        throw new RuntimeException(err);
    }

    private static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
}

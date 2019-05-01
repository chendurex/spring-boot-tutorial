package com.spring.boot.tutorial.util;

import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.*;

/**
 * @author cheny.huang
 * @date 2019-04-25 10:15.
 */
@Slf4j
public class IpAddressUtils {
    public static String getExternalIp() {
        Collection<String> ips = localAddress();
        for (String s : ips) {
            if (s.contains(".") && !"127.0.0.1".equals(s)) {
                return s;
            }
        }
        throw new RuntimeException("获取对外暴露地址失败");
    }

    public static String hostnameToIp(String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("解析[{}]地址失败，请检查是否配置dns或者hosts", hostname);
        }
        return null;
    }

    /**
     * 获取本机所有的网络IP，包括本地ip127.0.0.1或者局域网ip192.168.3.255
     */
    public static Collection<String> localAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            Set<String> localAddr = new HashSet<>();
            while(enumeration.hasMoreElements()) {
                Enumeration<InetAddress> address = enumeration.nextElement().getInetAddresses();
                while (address.hasMoreElements()) {
                    localAddr.add(address.nextElement().getHostAddress());
                }
            }
            return Collections.unmodifiableCollection(localAddr);
        } catch (Exception e) {
            log.error("", e);
            throw new IllegalStateException("获取本地地址失败", e);
        }
    }

    public static String hostFromUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return uri.getHost();
    }
}

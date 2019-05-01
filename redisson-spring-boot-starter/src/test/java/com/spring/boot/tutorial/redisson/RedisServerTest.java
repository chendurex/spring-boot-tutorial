package com.spring.boot.tutorial.redisson;

import org.junit.Test;
import redis.clients.jedis.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author cheny.huang
 * @date 2018-12-25 11:26.
 */
public class RedisServerTest {
    @Test
    public void testFetchNodes() {
        String nodes = "6d8d4d269dfcccd569aeebfc3fa327176f37e190 192.168.1.118:7006@17006 slave 4a14a1f3e7f27bc61ba3c13193e0d117a6fdcf2a 0 1545708019472 9 connected\n" +
                "4a14a1f3e7f27bc61ba3c13193e0d117a6fdcf2a 192.168.1.118:7003@17003 master - 0 1545708021000 7 connected 0-5461\n" +
                "d0cdde3731873f38c796a873fa6304702975d57c 192.168.1.118:7004@17004 slave ca87816faa4229b1984fbf4a43d72bdf30a9dab5 0 1545708021474 9 connected\n" +
                "21583339e2c0f121f00433486f27d0ccadd85a1e 192.168.1.118:7002@17002 master - 0 1545708022476 6 connected 10923-16383\n" +
                "f9205857e42f502fbb538d6a8a5007a9f8747451 192.168.1.118:7000@17000 myself,slave 4a14a1f3e7f27bc61ba3c13193e0d117a6fdcf2a 0 1545708021000 0 connected\n" +
                "ca87816faa4229b1984fbf4a43d72bdf30a9dab5 192.168.1.118:7001@17001 master - 0 1545708020000 9 connected 5462-10922\n" +
                "ce26fc963e5c17e4b09c42c1d31414d45af5d41c 192.168.1.118:7005@17005 slave 21583339e2c0f121f00433486f27d0ccadd85a1e 0 1545708020474 6 connected";
        System.out.println(fetchMaster(nodes));
        System.out.println(fetchSlave(nodes));
    }

    public void shutdownRandomOneOfSlave(String nodes) {
        List<String> node = fetchSlave(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("slave node is absent");
        }
        Collections.shuffle(node);
        shutdown(node.subList(0,1));
    }

    public void shutdownRandomOneOfMaster(String nodes) {
        List<String> node = fetchMaster(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("master node is absent");
        }
        Collections.shuffle(node);
        shutdown(node.subList(0,1));
    }

    public void shutdownTwoOfSlave(String nodes) {
        List<String> node = fetchSlave(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("slave node is absent");
        }
        Collections.shuffle(node);
        shutdown(node.size()>1 ? node.subList(0,2) : node.subList(0,1));
    }

    public void shutdownTwoOfMaster(String nodes) {
        List<String> node = fetchMaster(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("master node is absent");
        }
        Collections.shuffle(node);
        shutdown(node.size()>1 ? node.subList(0,2) : node.subList(0,1));
    }

    public void shutdownAllOfSlave(String nodes) {
        List<String> node = fetchSlave(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("slave node is absent");
        }
        Collections.shuffle(node);
        shutdown(node);
    }

    public void shutdownAllOfMaster(String nodes) {
        List<String> node = fetchMaster(nodes);
        if (node.isEmpty()) {
            throw new RuntimeException("slave node is absent");
        }
        Collections.shuffle(node);
        shutdown(node);
    }

    private List<String> fetchMaster(String nodes) {
        return fetch(nodes, "master");
    }

    private List<String> fetchSlave(String nodes) {
        return fetch(nodes, "slave");
    }

    private List<String> fetch(String nodes, String key) {
        List<String> fetch = new ArrayList<>();
        for (String s : nodes.split("\n")) {
            if (s.contains(key)) {
                fetch.add(s.substring(s.indexOf(" ")+1, s.indexOf("@")));
            }
        }
        return fetch;
    }

    private void shutdown(List<String> nodes) {
        for (int i=0;i<nodes.size();i++) {
            String host = nodes.get(i).split(":")[0];
            int port = Integer.valueOf(nodes.get(i).split(":")[1]);
            Client client = new Client(host, port);
            client.setPassword("123456");
            client.shutdown();
            try {
                // 奇奇怪怪的，如果不调用这个方法则不会关闭服务器，但是调用了，则直接报错，还未仔细研究为何
                System.out.println("-------------[" + client.getBulkReply() + "]-----------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (i == nodes.size() - 1) {
                return ;
            }
            // 一台一台的关闭，直到所有的master都确认关闭，master选举时间为15s，所以等待20s
            // 不能一下子把所有的master都关闭，否则slave无法完成晋升
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

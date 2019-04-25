package com.chen.spring.boot.druid.mybatis;

import com.chen.spring.boot.druid.DruidProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheny.huang
 * @date 2018-11-14 16:17.
 */
public class DruidPropertiesTest {
    @Test
    public void testCopy() {
        DruidProperties p = new DruidProperties();
        p.setUsername("1");
        p.setPassword("2");
        p.setUrl("3");
        p.setConnectionInitSqls(Arrays.asList("4", "5"));
        p.setConnectionProperties("6");
        p.setDriverClass("7");
        p.setFilters("8");
        p.setInitialSize(9);
        p.setLogAbandoned(true);
        p.setMaxActive(11);
        p.setMaxPoolPreparedStatementPerConnectionSize(12);
        p.setMaxWait(13);
        p.setMinEvictableIdleTimeMillis(14);
        p.setMinIdle(15);
        p.setPoolPreparedStatements(true);
        p.setTestOnBorrow(true);
        p.setTestOnReturn(true);
        Map<String, Map<String, String>> m = new HashMap<>(2);
        Map<String, String> stub = new HashMap<>();
        stub.put("username", "username1");
        stub.put("password", "password1");
        stub.put("url", "url1");

        Map<String, String> stub2 = new HashMap<>();
        stub2.put("username", "username2");
        stub2.put("password", "password2");
        stub2.put("url", "url2");

        m.put("multi1", stub);
        m.put("multi2", stub2);

        DruidProperties copy = p.copy();
        Assert.assertArrayEquals(copy.getConnectionInitSqls().toArray(), p.getConnectionInitSqls().toArray());
        Assert.assertEquals(copy.getConnectionProperties(), p.getConnectionProperties());
        Assert.assertEquals(copy.getInitialSize(), p.getInitialSize());
        Assert.assertEquals(copy.getLogAbandoned(), p.getLogAbandoned());
        Assert.assertEquals(copy.getMinIdle(), p.getMinIdle());
        Assert.assertNotEquals(copy.getPassword(), p.getPassword());
        Assert.assertNotEquals(copy.getUrl(), p.getUrl());
        Assert.assertNotEquals(copy.getUsername(), p.getUsername());
        Assert.assertNull(copy.getUrl());
        Assert.assertNull(copy.getUsername());
        Assert.assertNull(copy.getPassword());
    }
}

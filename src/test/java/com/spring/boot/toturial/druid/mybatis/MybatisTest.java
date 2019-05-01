package com.spring.boot.toturial.druid.mybatis;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:34.
 */
public class MybatisTest extends BaseApplication {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        String v = "test";
        String u = "testu";
        User user = new User();
        user.setName(v);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertTrue(v.equals(userMapper.select(id).getName()));
        userMapper.update(u, id);
        Assert.assertTrue(u.equals(userMapper.select(id).getName()));
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testPage() {
        List<User> user = userMapper.list(1, 1);
        Assert.assertTrue(user.size() == 1);

        user = userMapper.list(1, 10);
        Assert.assertTrue(user.size() == 10);
    }
}

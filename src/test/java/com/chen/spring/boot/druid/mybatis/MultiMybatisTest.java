package com.chen.spring.boot.druid.mybatis;

import com.chen.spring.boot.druid.MultipleDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * @author cheny.huang
 * @date 2018-08-15 10:34.
 */
public class MultiMybatisTest extends BaseApplication {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testCheckMultiSource() {
        Assert.assertNull(userMapper.select(9999));
        // 开启检查多数据源功能，如果数据源不存在则抛出异常
        MultipleDataSource.route("not exist");
        Assert.assertNull(userMapper.select(9999));

    }

    @Test
    public void testMultiSelect() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());

        // 切换到第一个数据源
        MultipleDataSource.route("spring");
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());

        // 切换到第二个数据源
        MultipleDataSource.route("cloud");
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));

        MultipleDataSource.route("spring");
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));

        // 回到默认数据源
        MultipleDataSource.reset();
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testMulti2() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());

        // 切换到第一个数据源
        MultipleDataSource.routeAtMulti("spring");
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 切换到第二个数据源
        MultipleDataSource.routeAtMulti("cloud");
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));

        MultipleDataSource.routeAtMulti("spring");
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));

        // 回到默认数据源
        MultipleDataSource.reset();
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testMultiAtOnce() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());

        // 切换数据源
        MultipleDataSource.routeAtOnce("spring");
        Assert.assertNull(userMapper.select(id));
        // 回到默认数据库中，所以可以查到数据
        Assert.assertEquals(v, userMapper.select(id).getName());
        MultipleDataSource.routeAtOnce("spring");
        userMapper.insert(user);
        // 删除默认数据库中数据
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
        MultipleDataSource.routeAtOnce("spring");
        Assert.assertEquals(v, userMapper.select(id).getName());
        MultipleDataSource.routeAtOnce("spring");
        userMapper.delete(id);
        MultipleDataSource.routeAtOnce("spring");
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testMasterSlave() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 切换到从数据库
        MultipleDataSource.slave();
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
        // 切换到默认数据库
        MultipleDataSource.reset();
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testMasterSlave2() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 切换到从数据库
        MultipleDataSource.slaveAtMulti();
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
        // 切换到默认数据库
        MultipleDataSource.reset();
        Assert.assertEquals(v, userMapper.select(id).getName());
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testMasterSlaveAtOnce() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 切换到从数据库
        MultipleDataSource.slaveAtOnce();
        Assert.assertNull(userMapper.select(id));
        Assert.assertNotNull(userMapper.select(id));
        MultipleDataSource.slaveAtOnce();
        userMapper.insert(user);
        MultipleDataSource.slaveAtOnce();
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 删除主库数据
        userMapper.delete(id);
        Assert.assertNull(userMapper.select(id));
        MultipleDataSource.reset();
        Assert.assertNull(userMapper.select(id));
        MultipleDataSource.slaveAtOnce();
        userMapper.delete(id);
        MultipleDataSource.slaveAtOnce();
        Assert.assertNull(userMapper.select(id));
    }

    @Test
    public void testCombination() {
        String v = "test";
        User user = new User();
        user.setName(v);
        Random random = new Random(Integer.MAX_VALUE);
        int i;
        for (;;) {
            i = random.nextInt();
            if (i > 0) {
                break;
            }
        }
        user.setId(i);
        int count = userMapper.insert(user);
        Assert.assertTrue(count > 0);
        int id = user.getId();
        Assert.assertEquals(v, userMapper.select(id).getName());
        // 切换到从数据库
        MultipleDataSource.slaveAtMulti();
        Assert.assertNull(userMapper.select(id));
        userMapper.insert(user);
        Assert.assertEquals(v, userMapper.select(id).getName());

        MultipleDataSource.slaveAtOnce();
        userMapper.delete(id);
        // 默认切换到主库，所以能查到数据
        Assert.assertNotNull(userMapper.select(id));
        MultipleDataSource.slaveAtOnce();
        Assert.assertNull(userMapper.select(id));
        userMapper.delete(id);
        MultipleDataSource.reset();
        Assert.assertNull(userMapper.select(id));
    }
}

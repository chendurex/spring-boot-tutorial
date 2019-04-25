package com.chen.spring.boot.druid.mybatis;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author cheny.huang
 * @date 2018-08-02 17:50.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
// 开启事务会导致多数据源失效
//@Transactional
public class BaseApplication {
}

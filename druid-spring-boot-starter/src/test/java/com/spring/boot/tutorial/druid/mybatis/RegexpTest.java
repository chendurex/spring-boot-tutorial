package com.spring.boot.tutorial.druid.mybatis;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tim.wei
 * @description
 * @date Created on 2018/11/7.
 */

public class RegexpTest {


    @Test
    public void test(){

        List<String> configList = new ArrayList<>(4);
        configList.add("spring.druid.acc.username") ;
        configList.add("spring.druid.username") ;
        configList.add("spring.druid.pem.username") ;
        configList.add("spring.druid.eda.username") ;

        configList.forEach(this::test2);
    }


    public void test2(String src){
        Pattern log_ptn = Pattern.compile("spring.druid.([\\s\\S]+).username");
        Matcher log_m = log_ptn.matcher(src);
        while (log_m.find()) {
            System.out.println("multi = " + log_m.group(1));
        }
    }
}

package com.spring.boot.tutorial.advice.print;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.ref.SoftReference;

/**
 * @author chen
 * @description 因为objectMapper在各种打印日志时都需要使用，为了防止每次打印日志都创建一个新的对象
 *                这里将对象缓存到ThreadLocal里面，因为ThreadLocal是跟线程绑定，而本项目的普通请求是通过tomcat的线程池获取线程
 *                RPC调用是采用dubbo的线程池，这样保证线程安全的同时也保证了创建对象开销的减少
 *
 *                使用SoftReference的理由是：因为对象是与线程绑定，但是线程是由线程池管理，每次线程执行完毕不一定被销毁，有可能
 *                是被线程池回收利用，这样导致对象也无法释放，为了防止出现内存溢出，所以采用了软引用，如果出现内存不够的情况，
 *                会自动清理对象
 *
 *                为了给使用者一个最简单的操作，而且防止出现不可控的情况，禁止了 set、remove操作，使用者只管理如何获取即可
 *
 * ObjectMapper在序列化时候会创建一个JsonGenerator对象，所以是线程安全的，所以直接把它当常量对象使用就好了，下面这个实现完全是属于装逼
 * @date 2017/3/17 14:06
 * updated 2018/8/04
 */
public class ObjectMapperUtils {

    private static final ObjectMapperThreadLocal OMT = new ObjectMapperThreadLocal();

    public static ObjectMapper get() {
        return OMT.get().get();
    }

    private static class ObjectMapperThreadLocal extends ThreadLocal<SoftReference<ObjectMapper>> {

        @Override
        protected SoftReference<ObjectMapper> initialValue() {
            return new SoftReference<>(new ObjectMapper());
        }

        @Override
        public void set(SoftReference<ObjectMapper> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SoftReference<ObjectMapper> get() {
            SoftReference<ObjectMapper> softReference = super.get();
            if (softReference.get() == null) {
                softReference = new SoftReference<>(new ObjectMapper());
                super.set(softReference);
            }
            return softReference;
        }
    }

}

package com.spring.boot.tutorial;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware{
	/** 
	 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext. 
	 *  
	 */
	private static ApplicationContext applicationContext; 
		  
	/** 
	* 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量. 
	*/
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {  
	SpringContextHolder.applicationContext = applicationContext;
	}  
	  
	/**
	* 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型. 
	*/  
	@SuppressWarnings("unchecked")  
	public static <T> T getBean(String name) {  
	checkApplicationContext();  
	return (T) applicationContext.getBean(name);  
	}  
	  
	/** 
	* 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型. 
	*/
	public static <T> T getBean(Class<T> clazz) {  
	checkApplicationContext();  
	return applicationContext.getBean(clazz);
	}  
	  
	private static void checkApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("applicationContext未注入,请在applicationContext.xml中定义SpringContextHolder");
		}
	} 
}

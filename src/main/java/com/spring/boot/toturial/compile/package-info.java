/**
 * <p>因为开启了编译效果，导致当前项目编译的时候也会进行检查，导致无法编译通过，需要在一个子项目中关闭编译检查功能</p>
 * <code>
 *     <plugins>
         <plugin>
         <artifactId>maven-compiler-plugin</artifactId>
         <version>3.1</version>
         <configuration>
         <source>1.8</source>
         <target>1.8</target>
         <encoding>UTF-8</encoding>
         <!-- 编译的时候不进行注解编译，防止无法编译代码 -->
         <compilerArgument>-proc:none</compilerArgument>
         </configuration>
         </plugin>
         </plugins>
 * </code>
 * @author cheny.huang
 * @date 2019-04-25 11:03.
 */
package com.spring.boot.toturial.compile;
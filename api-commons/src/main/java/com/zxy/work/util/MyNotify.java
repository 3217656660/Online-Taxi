package com.zxy.work.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于表示未来需要改进重构以及一些信息等,用于类和方法等。生命周期：编译阶段
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MyNotify {
    String value() default "";
}

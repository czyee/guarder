package org.czyee.guarder.annotated.annotation;

import java.lang.annotation.*;

/**
 * 模块标识,当前url指向一个链接
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Module {

	String id();

	String name() default "";

	String node() default "";

	int sort() default 0;
}

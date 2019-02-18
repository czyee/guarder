package org.czyee.guarder.annotated.annotation;

import java.lang.annotation.*;

/**
 * 权限,当前方法是有权限的
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Perm {

	/**
	 * 权限名称,授权时用来在前端显示
	 * @return
	 */
	String name() default "";

	/**
	 * 权限所属的模块,用来对权限进行分类
	 * @return
	 */
	String module() default "";
}

package com.zzl.platform.auth.aspect;

import java.lang.annotation.*;

/**
 * 角色校验注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 角色编码
     */
    String value();

    /**
     * 角色描述
     */
    String desc() default "";
}

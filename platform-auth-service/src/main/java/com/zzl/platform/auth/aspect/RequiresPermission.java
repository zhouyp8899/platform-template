package com.zzl.platform.auth.aspect;

import java.lang.annotation.*;

/**
 * 权限校验注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 权限编码
     */
    String value();

    /**
     * 权限描述
     */
    String desc() default "";
}

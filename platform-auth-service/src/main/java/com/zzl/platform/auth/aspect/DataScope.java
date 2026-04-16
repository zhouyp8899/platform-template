package com.zzl.platform.auth.aspect;

import com.zzl.platform.auth.enums.DataScopeType;

import java.lang.annotation.*;

/**
 * 数据权限注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门别名
     */
    String deptAlias() default "d";

    /**
     * 用户别名
     */
    String userAlias() default "u";

    /**
     * 数据权限范围（默认使用用户的数据权限配置）
     */
    DataScopeType value() default DataScopeType.CUSTOM;
}

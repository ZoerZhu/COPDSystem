package com.copd.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 需要的角色，默认为患者
     */
    String[] value() default "PATIENT";

    /**
     * 是否需要管理员权限
     */
    boolean admin() default false;
}

package me.stinper.jwtauth.core.security.permission.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface PermissionScan {
    String[] packages();
}

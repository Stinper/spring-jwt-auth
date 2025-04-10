package me.stinper.jwtauth.core.security.permission.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationPermission {
    String permission();
    String description() default "";
}

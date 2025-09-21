package com.example.backendplantshop.security.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) //áp dụng được cho method/class
@Retention(RetentionPolicy.RUNTIME) //được giữ lại trong qá trình runtime
@PreAuthorize("hasRole('ADMIN')")
public @interface RequireAdmin { //định nghĩa anno
}


package com.example.backendplantshop.service.intf;

import com.example.backendplantshop.service.impl.EmailOrPhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailOrPhoneValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmailOrPhone {
    String message() default "Phải cung cấp ít nhất email hoặc số điện thoại";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

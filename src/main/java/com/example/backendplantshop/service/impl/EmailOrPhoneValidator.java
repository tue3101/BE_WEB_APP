package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.dto.request.users.LoginDtoRequest;
import com.example.backendplantshop.dto.request.users.RegisterDtoRequest;
import com.example.backendplantshop.dto.request.users.UserDtoRequest;
import com.example.backendplantshop.service.intf.ValidEmailOrPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOrPhoneValidator implements ConstraintValidator<ValidEmailOrPhone, Object> {

    private String messageTemplate;

    @Override
    public void initialize(ValidEmailOrPhone constraintAnnotation) {
        this.messageTemplate = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }
        
        String email = null;
        String phone = null;
        
        if (dto instanceof RegisterDtoRequest) {
            RegisterDtoRequest registerDto = (RegisterDtoRequest) dto;
            email = registerDto.getEmail();
            phone = registerDto.getPhone_number();
        } else if (dto instanceof LoginDtoRequest) {
            LoginDtoRequest loginDto = (LoginDtoRequest) dto;
            email = loginDto.getEmail();
            phone = loginDto.getPhone_number();
        } else if (dto instanceof UserDtoRequest) {
            UserDtoRequest userDto = (UserDtoRequest) dto;
            email = userDto.getEmail();
            phone = userDto.getPhone_number();
        }
        
        boolean hasEmail = email != null && !email.trim().isEmpty();
        boolean hasPhone = phone != null && !phone.trim().isEmpty();
        if (hasEmail || hasPhone) {
            return true;
        }

        // Gắn lỗi vào cả hai field để GlobalExceptionHandler thu thập FieldError
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode("email")
                .addConstraintViolation();
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode("phone_number")
                .addConstraintViolation();
        return false;
    }
}

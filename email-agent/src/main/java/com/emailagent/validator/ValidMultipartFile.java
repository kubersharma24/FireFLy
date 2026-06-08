package com.emailagent.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipartFileValidator.class)
public @interface ValidMultipartFile {
    String message() default "File is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
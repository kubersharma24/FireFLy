package com.emailagent.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileValidator
        implements ConstraintValidator<ValidMultipartFile, MultipartFile> {

    @Override
    public boolean isValid(
            MultipartFile file,
            ConstraintValidatorContext context) {

        return file != null && !file.isEmpty();
    }
}
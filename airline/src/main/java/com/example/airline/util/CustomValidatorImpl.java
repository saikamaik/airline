package com.example.airline.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomValidatorImpl implements CustomValidator {
    private final Validator validator;

    public CustomValidatorImpl(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Set<ConstraintViolation<Object>> validate(Object objectDto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(objectDto);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation errors: " + errorMessages);
        }
        return violations;
    }
}

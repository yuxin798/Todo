package com.todo.constraints.validators;

import com.todo.constraints.ValidName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {
    private int min;
    private int max;
    private String message;
    private Class<?>[] groups;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
        message = constraintAnnotation.message();
        groups = constraintAnnotation.groups();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        value = value.trim();
        return value.length() >= min && value.length() <= max;
    }
}

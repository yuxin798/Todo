package com.todo.constraints.validators;

import com.todo.constraints.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Date;

public class ValidDateValidator implements ConstraintValidator<ValidDate, Date> {
    private long before;
    private long after;
    private String message;
    private Class<?>[] groups;

    @Override
    public void initialize(ValidDate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        before = constraintAnnotation.before();
        after = constraintAnnotation.after();
        message = constraintAnnotation.message();
        groups = constraintAnnotation.groups();
    }

    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
        if (value == null) return true;

        long epochSecond = value.toInstant().getEpochSecond();
        if (before > epochSecond) return false;
        if (after < epochSecond) return false;

        return true;
    }
}

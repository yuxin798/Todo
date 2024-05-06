package com.todo.constraints.validators;

import com.todo.constraints.AvatarLink;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class AvatarLinkValidator implements ConstraintValidator<AvatarLink, String> {
    private String message;
    private Class<?>[] groups;

    @Override
    public void initialize(AvatarLink constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        message = constraintAnnotation.message();
        groups = constraintAnnotation.groups();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return Pattern.matches("https?://\\S+/todo-bucket/.+\\.(png|jpeg|jpg|gif)", value);
    }
}

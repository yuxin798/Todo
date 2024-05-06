package com.todo.constraints;

import com.todo.constraints.validators.ValidDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = ValidDateValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ValidDate {
    long before() default Long.MIN_VALUE;

    long after() default Long.MIN_VALUE;

    String message() default "日期时间不合法";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

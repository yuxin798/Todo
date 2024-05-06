package com.todo.constraints;

import com.todo.constraints.validators.AvatarLinkValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 形如：
 * <ul>
 *     <li>
 *         http://8.130.17.7:9000/todo-bucket/1.jpeg
 *     </li>
 *     <li>
 *         https://a.com/todo-bucket/1.jpeg
 *     </li>
 * </ul>
 */
@Constraint(validatedBy = AvatarLinkValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface AvatarLink {
    String message() default "必须是规范的头像链接";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}

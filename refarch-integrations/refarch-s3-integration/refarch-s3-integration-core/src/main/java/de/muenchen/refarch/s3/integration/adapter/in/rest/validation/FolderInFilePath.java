package de.muenchen.refarch.s3.integration.adapter.in.rest.validation;

import static java.lang.annotation.ElementType.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = FolderInFilePathValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FolderInFilePath {

    String message() default "There is no folder in filepath. " +
            "It is not allowed to save a file directly within the root directory.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

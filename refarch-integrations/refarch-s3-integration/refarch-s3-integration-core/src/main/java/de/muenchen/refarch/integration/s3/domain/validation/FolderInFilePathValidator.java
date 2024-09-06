package de.muenchen.refarch.integration.s3.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class FolderInFilePathValidator implements ConstraintValidator<FolderInFilePath, String> {

    public static final String SEPARATOR = "/";

    /**
     * Checks if the file path contains a folder.
     *
     * @param pathToFile the file path.
     * @param context context in which the constraint is evaluated
     * @return false if the filepath contains no folder, <code>true</code> otherwise.
     */
    @Override
    public boolean isValid(final String pathToFile, final ConstraintValidatorContext context) {
        final String pathToFolder = StringUtils.substringBeforeLast(pathToFile, SEPARATOR);
        return StringUtils.contains(pathToFile, SEPARATOR) && StringUtils.isNotEmpty(pathToFolder);
    }

}

package de.muenchen.refarch.integration.s3.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.refarch.integration.s3.adapter.in.rest.validation.FolderInFilePathValidator;
import org.junit.jupiter.api.Test;

class FolderInFilePathValidatorTest {

    private final FolderInFilePathValidator folderInFilePathValidator = new FolderInFilePathValidator();

    @Test
    void isValid() {
        assertThat(this.folderInFilePathValidator.isValid("folder/file.txt", null)).isTrue();
        assertThat(this.folderInFilePathValidator.isValid("folder/subfolder/file.txt", null)).isTrue();
        assertThat(this.folderInFilePathValidator.isValid("file.txt", null)).isFalse();
        assertThat(this.folderInFilePathValidator.isValid("", null)).isFalse();
        assertThat(this.folderInFilePathValidator.isValid(null, null)).isFalse();
    }
}

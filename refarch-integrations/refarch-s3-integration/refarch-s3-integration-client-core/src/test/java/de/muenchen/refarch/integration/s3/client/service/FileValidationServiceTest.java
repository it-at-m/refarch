package de.muenchen.refarch.integration.s3.client.service;

import static org.junit.jupiter.api.Assertions.*;

import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.exception.NoFileTypeException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class FileValidationServiceTest {

    private FileValidationService fileValidationService;

    @BeforeEach
    void setUp() {
        final SupportedFileExtensions fileExtensions = new SupportedFileExtensions();
        fileExtensions.put("pdf", "application/pdf");
        fileExtensions.put("txt", "text/plain");
        fileValidationService = new FileValidationService(fileExtensions, DataSize.ofKilobytes(10), DataSize.ofMegabytes(5));
    }

    @Test
    void testIsSupported() {
        assertTrue(fileValidationService.isSupported("pdf"));
        assertFalse(fileValidationService.isSupported("exe"));
    }

    @Test
    void testGetFileExtension() {
        assertEquals("pdf", fileValidationService.getFileExtension("application/pdf"));
    }

    @Test
    void testGetFileExtensionThrowsException() {
        assertThrows(NoFileTypeException.class, () -> fileValidationService.getFileExtension("application/unknown"));
    }

    @Test
    void testDetectFileType() {
        final byte[] fileContent = "Hello, universe!".getBytes();
        assertEquals("text/plain", fileValidationService.detectFileType(fileContent));
    }

    @Test
    void testSupportCheckWithEmptyMap() {
        final FileValidationService emptyService = new FileValidationService(new SupportedFileExtensions(), DataSize.ofMegabytes(100),
                DataSize.ofMegabytes(500));
        assertTrue(emptyService.isSupported("anyType"));
    }

    @Test
    void testIsValidFileSizeByteArray() {
        assertTrue(fileValidationService.isValidFileSize(new byte[] { 1 }));
    }

    @Test
    void testIsValidFileSizeLong() {
        assertFalse(fileValidationService.isValidFileSize(10000000L)); // Assuming maxFileSize is set to 10MB
    }

    @Test
    void testGetTotalBatchSize() {
        Map<String, Long> fileSizesWithPaths = new HashMap<>();
        fileSizesWithPaths.put("path/to/file1", 1024L);
        fileSizesWithPaths.put("path/to/file2", 2048L);

        DataSize expectedTotalSize = DataSize.ofBytes(3072); // 1024 + 2048

        assertEquals(expectedTotalSize, fileValidationService.getTotalBatchSize(fileSizesWithPaths));
    }

    @Test
    void testGetOversizedFiles() {
        Map<String, Long> fileSizesWithPaths = new HashMap<>();
        fileSizesWithPaths.put("path/to/smallFile.txt", 500L);
        fileSizesWithPaths.put("path/to/largeFile.pdf", 15000000L);
        fileSizesWithPaths.put("path/to/mediumFile.docx", 7000L);

        Map<String, Long> expectedOversizedFiles = new HashMap<>();
        expectedOversizedFiles.put("path/to/largeFile.pdf", 15000000L);

        Map<String, Long> oversizedFiles = fileValidationService.getOversizedFiles(fileSizesWithPaths);

        assertEquals(expectedOversizedFiles, oversizedFiles);
    }

}

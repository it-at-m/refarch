package de.muenchen.refarch.integration.dms.adapter.out.s3;

import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.exception.FileSizeValidationException;
import de.muenchen.refarch.integration.s3.client.exception.FileTypeValidationException;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.unit.DataSize;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3AdapterTest {

    private static final DataSize ALLOWED_FILE_SIZE = DataSize.ofMegabytes(100);
    private static final DataSize ALLOWED_BATCH_SIZE = DataSize.ofMegabytes(110);
    private static final long TOO_LARGE_FILE_SIZE = ALLOWED_FILE_SIZE.toBytes() + DataSize.ofMegabytes(1L).toBytes(); // 1 Mbyte over allowed
    public static final String PDF = "pdf";
    public static final String PNG = "png";
    public static final String PDF_PATH = "files/test/test-pdf.pdf";
    public static final String PNG_PATH = "files/test/test-png.png";
    public static final String PDF_NAME = "test-pdf";
    public static final String PNG_NAME = "test-png";
    public static final String SOME_ERROR = "Some error";
    public static final String FOLDER_PATH = "test/";
    public static final String FULL_WORD_PATH = "files/test/test-word.docx";

    private final DocumentStorageFileRepository documentStorageFileRepository = mock(DocumentStorageFileRepository.class);

    private final DocumentStorageFolderRepository documentStorageFolderRepository = mock(DocumentStorageFolderRepository.class);

    private final Map<String, String> supportedExtensions = new SupportedFileExtensions(Map.of(PDF, "application/pdf",
            PNG, "image/png",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));

    private final FileValidationService fileValidationService = new FileValidationService(supportedExtensions, ALLOWED_FILE_SIZE, ALLOWED_BATCH_SIZE);

    private S3Adapter s3Adapter;

    @BeforeEach
    void setup() {
        s3Adapter = new S3Adapter(documentStorageFileRepository, documentStorageFolderRepository, fileValidationService);
    }

    @Test
    void testLoadFileFromFilePath()
            throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final List<String> filePaths = List.of(PDF_PATH, PNG_PATH);

        final byte[] testPdf = new ClassPathResource(PDF_PATH).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(PNG_PATH).getInputStream().readAllBytes();

        when(documentStorageFileRepository.getFile(PDF_PATH, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(PNG_PATH, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFileSize(anyString())).thenReturn(1_000_000L);

        final List<Content> contents = this.s3Adapter.loadFiles(filePaths);

        final Content pdfContent = new Content(PDF, PDF_NAME, testPdf);
        final Content pngContent = new Content(PNG, PNG_NAME, testPng);

        assertTrue(contents.contains(pdfContent));
        assertTrue(contents.contains(pngContent));
    }

    @Test
    void testLoadFileFromFolderPath()
            throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final List<String> paths = List.of(FOLDER_PATH);

        final Set<String> filesPaths = new HashSet<>(List.of(PDF_PATH, PNG_PATH, FULL_WORD_PATH));

        final byte[] testPdf = new ClassPathResource(PDF_PATH).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(PNG_PATH).getInputStream().readAllBytes();
        final byte[] testWord = new ClassPathResource(FULL_WORD_PATH).getInputStream().readAllBytes();

        when(documentStorageFolderRepository.getAllFilesInFolderRecursively(FOLDER_PATH)).thenReturn(filesPaths);

        when(documentStorageFileRepository.getFile(PDF_PATH, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(PNG_PATH, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFile(FULL_WORD_PATH, 3)).thenReturn(testWord);
        when(documentStorageFolderRepository.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Map.of("", 1_000_000L));

        final List<Content> contents = this.s3Adapter.loadFiles(paths);

        final Content pdfContent = new Content(PDF, PDF_NAME, testPdf);
        final Content pngContent = new Content(PNG, PNG_NAME, testPng);
        final Content wordContent = new Content("docx", "test-word", testWord);

        assertTrue(contents.contains(pdfContent));
        assertTrue(contents.contains(pngContent));
        assertTrue(contents.contains(wordContent));
    }

    @Test
    void testLoadFileFromFilePathThrowsDocumentStorageException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String pdfPath = "test/test-pdf.pdf";

        final List<String> filePaths = List.of(pdfPath);

        when(documentStorageFileRepository.getFileSize(anyString())).thenReturn(1_000_000L);
        when(documentStorageFileRepository.getFile(pdfPath, 3)).thenThrow(
                new DocumentStorageException(SOME_ERROR, new RuntimeException(SOME_ERROR)));

        final DocumentStorageException documentStorageException = assertThrows(DocumentStorageException.class, () -> this.s3Adapter.loadFiles(filePaths));

        assertEquals(SOME_ERROR, documentStorageException.getMessage());
    }

    @Test
    void testLoadFileFromFolderPathThrowsDocumentStorageServerErrorException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String folderPath = FOLDER_PATH;

        final List<String> filePaths = List.of(folderPath);

        when(documentStorageFolderRepository.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Map.of("", 1_000_000L));
        when(documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath)).thenThrow(
                new DocumentStorageServerErrorException(SOME_ERROR, new RuntimeException(SOME_ERROR)));

        final DocumentStorageException documentStorageException = assertThrows(DocumentStorageException.class, () -> this.s3Adapter.loadFiles(filePaths));

        final String expectedMessage = "An folder could not be loaded from url: " + folderPath;
        final String actualMessage = documentStorageException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testLoadFileFromFilePathThrowsUnsupportedFileTypeException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException, IOException {

        final String htmlPath = "files/fail/test-html.html";

        final List<String> filePaths = List.of(htmlPath);

        final byte[] testHtml = new ClassPathResource(htmlPath).getInputStream().readAllBytes();

        when(documentStorageFileRepository.getFile(htmlPath, 3)).thenReturn(testHtml);
        when(documentStorageFileRepository.getFileSize(anyString())).thenReturn(1_000_000L);

        final String expectedMessage = "The type of this file is not supported: " + htmlPath;

        assertThatThrownBy(() -> s3Adapter.loadFiles(filePaths))
                .isInstanceOf(FileTypeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

    @Test
    void testLoadFilesThrowsFileSizeValidationExceptionDueToInvalidBatchSize() throws Exception {
        final String pathLargeFile = "path/to/largeFile";
        final String pathSmallFile = "path/to/smallFile";
        final List<String> filePaths = Arrays.asList(pathLargeFile, pathSmallFile);

        when(documentStorageFileRepository.getFileSize(eq(pathLargeFile))).thenReturn(ALLOWED_FILE_SIZE.toBytes());
        when(documentStorageFileRepository.getFileSize(eq(pathSmallFile))).thenReturn(
                DataSize.ofMegabytes(20).toBytes());

        final DataSize sum = DataSize.ofBytes(ALLOWED_FILE_SIZE.toBytes() + DataSize.ofMegabytes(20).toBytes());
        final String expectedMessage = String.format("Batch size of %d MB is too large. Allowed are %d MB.", sum.toMegabytes(),
                ALLOWED_BATCH_SIZE.toMegabytes());

        assertThatThrownBy(() -> s3Adapter.loadFiles(filePaths))
                .isInstanceOf(FileSizeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

    @Test
    void testLoadFilesThrowsFileSizeValidationExceptionDueToFileExceedingMaxSize() throws Exception {
        final String pathLargeFile = "path/to/largeFile";
        final String pathSmallFile = "path/to/smallFile";
        final List<String> filePaths = Arrays.asList(pathLargeFile, pathSmallFile);

        when(documentStorageFileRepository.getFileSize(eq(pathLargeFile))).thenReturn(TOO_LARGE_FILE_SIZE);
        when(documentStorageFileRepository.getFileSize(eq(pathSmallFile))).thenReturn(10_240L);

        final String expectedMessage = String.format("The following files exceed the maximum size of %d MB:%n%s: %d MB", ALLOWED_FILE_SIZE.toMegabytes(),
                pathLargeFile, DataSize.ofBytes(TOO_LARGE_FILE_SIZE).toMegabytes());

        assertThatThrownBy(() -> s3Adapter.loadFiles(filePaths))
                .isInstanceOf(FileSizeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

    @Test
    void testTransferContent() throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String folderPathWithSlash = "folder/";
        final String folderPathWithoutSlash = "folder";
        final byte[] testPdf = new ClassPathResource(PDF_PATH).getInputStream().readAllBytes();
        final Content pdfContent = new Content(PDF, PDF_NAME, testPdf);
        final String fullPath = String.format("%s/%s", folderPathWithoutSlash, "test-pdf.pdf");
        final String fullPathWrong = String.format("%s//%s", folderPathWithoutSlash, "test-pdf.pdf");

        this.s3Adapter.transferContent(List.of(pdfContent), folderPathWithSlash);
        this.s3Adapter.transferContent(List.of(pdfContent), folderPathWithoutSlash);

        verify(documentStorageFileRepository, never()).saveFile(eq(fullPathWrong), any(), anyInt());
        verify(documentStorageFileRepository, times(2)).saveFile(eq(fullPath), any(), anyInt());
    }
}

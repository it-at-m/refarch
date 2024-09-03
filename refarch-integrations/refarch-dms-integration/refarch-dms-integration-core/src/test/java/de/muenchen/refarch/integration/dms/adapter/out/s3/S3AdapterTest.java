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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class S3AdapterTest {

    private static final DataSize ALLOWED_FILE_SIZE = DataSize.ofMegabytes(100);
    private static final DataSize ALLOWED_BATCH_SIZE = DataSize.ofMegabytes(110);
    private static final long TOO_LARGE_FILE_SIZE = ALLOWED_FILE_SIZE.toBytes() + DataSize.ofMegabytes(1L).toBytes(); // 1 Mbyte over allowed

    private final DocumentStorageFileRepository documentStorageFileRepository = mock(DocumentStorageFileRepository.class);

    private final DocumentStorageFolderRepository documentStorageFolderRepository = mock(DocumentStorageFolderRepository.class);

    private final SupportedFileExtensions supportedExtensions = new SupportedFileExtensions(Map.of("pdf", "application/pdf",
            "png", "image/png",
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

        final String pdfPath = "files/test/test-pdf.pdf";
        final String pngPath = "files/test/digiwf_logo.png";

        final List<String> filePaths = List.of(pdfPath, pngPath);

        final byte[] testPdf = new ClassPathResource(pdfPath).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(pngPath).getInputStream().readAllBytes();

        when(documentStorageFileRepository.getFile(pdfPath, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(pngPath, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFileSize(anyString())).thenReturn(1_000_000L);

        final List<Content> contents = this.s3Adapter.loadFiles(filePaths);

        final Content pdfContent = new Content("pdf", "test-pdf", testPdf);
        final Content pngContent = new Content("png", "digiwf_logo", testPng);

        assertTrue(contents.contains(pdfContent));
        assertTrue(contents.contains(pngContent));
    }

    @Test
    void testLoadFileFromFilePathWithStorageUrl()
            throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String pdfPath = "files/test/test-pdf.pdf";
        final String pngPath = "files/test/digiwf_logo.png";

        final List<String> filePaths = List.of(pdfPath, pngPath);

        final byte[] testPdf = new ClassPathResource(pdfPath).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(pngPath).getInputStream().readAllBytes();

        when(documentStorageFileRepository.getFile(pdfPath, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(pngPath, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFileSize(anyString())).thenReturn(1_000_000L);

        final List<Content> contents = this.s3Adapter.loadFiles(filePaths);

        final Content pdfContent = new Content("pdf", "test-pdf", testPdf);
        final Content pngContent = new Content("png", "digiwf_logo", testPng);

        assertTrue(contents.contains(pdfContent));
        assertTrue(contents.contains(pngContent));
    }

    @Test
    void testLoadFileFromFolderPath()
            throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String folderPath = "test/";

        final String pdfPath = "files/test/test-pdf.pdf";
        final String pngPath = "files/test/digiwf_logo.png";
        final String fullWordPath = "files/test/test-word.docx";

        final List<String> paths = List.of(folderPath);

        final Set<String> filesPaths = new HashSet<>(List.of(pdfPath, pngPath, fullWordPath));

        final byte[] testPdf = new ClassPathResource(pdfPath).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(pngPath).getInputStream().readAllBytes();
        final byte[] testWord = new ClassPathResource(fullWordPath).getInputStream().readAllBytes();

        when(documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath)).thenReturn(filesPaths);

        when(documentStorageFileRepository.getFile(pdfPath, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(pngPath, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFile(fullWordPath, 3)).thenReturn(testWord);
        when(documentStorageFolderRepository.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Map.of("", 1_000_000L));

        final List<Content> contents = this.s3Adapter.loadFiles(paths);

        final Content pdfContent = new Content("pdf", "test-pdf", testPdf);
        final Content pngContent = new Content("png", "digiwf_logo", testPng);
        final Content wordContent = new Content("docx", "test-word", testWord);

        assertTrue(contents.contains(pdfContent));
        assertTrue(contents.contains(pngContent));
        assertTrue(contents.contains(wordContent));
    }

    @Test
    void testLoadFileFromFolderPathWithStorageUrl()
            throws IOException, DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String folderPath = "test/";

        final String pdfPath = "files/test/test-pdf.pdf";
        final String pngPath = "files/test/digiwf_logo.png";
        final String fullWordPath = "files/test/test-word.docx";

        final List<String> paths = List.of(folderPath);

        Set<String> filesPaths = new HashSet<>(List.of(pdfPath, pngPath, fullWordPath));

        final byte[] testPdf = new ClassPathResource(pdfPath).getInputStream().readAllBytes();
        final byte[] testPng = new ClassPathResource(pngPath).getInputStream().readAllBytes();
        final byte[] testWord = new ClassPathResource(fullWordPath).getInputStream().readAllBytes();

        when(documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath)).thenReturn(filesPaths);

        when(documentStorageFileRepository.getFile(pdfPath, 3)).thenReturn(testPdf);
        when(documentStorageFileRepository.getFile(pngPath, 3)).thenReturn(testPng);
        when(documentStorageFileRepository.getFile(fullWordPath, 3)).thenReturn(testWord);
        when(documentStorageFolderRepository.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Map.of("", 1_000_000L));

        final List<Content> contents = this.s3Adapter.loadFiles(paths);

        final Content pdfContent = new Content("pdf", "test-pdf", testPdf);
        final Content pngContent = new Content("png", "digiwf_logo", testPng);
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
                new DocumentStorageException("Some error", new RuntimeException("Some error")));

        DocumentStorageException documentStorageException = assertThrows(DocumentStorageException.class, () -> this.s3Adapter.loadFiles(filePaths));

        String expectedMessage = "An file could not be loaded from url: " + pdfPath;
        String actualMessage = documentStorageException.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testLoadFileFromFolderPathThrowsDocumentStorageServerErrorException()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {

        final String folderPath = "test/";

        final List<String> filePaths = List.of(folderPath);

        when(documentStorageFolderRepository.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Map.of("", 1_000_000L));
        when(documentStorageFolderRepository.getAllFilesInFolderRecursively(folderPath)).thenThrow(
                new DocumentStorageServerErrorException("Some error", new RuntimeException("Some error")));

        DocumentStorageException documentStorageException = assertThrows(DocumentStorageException.class, () -> this.s3Adapter.loadFiles(filePaths));

        String expectedMessage = "An folder could not be loaded from url: " + folderPath;
        String actualMessage = documentStorageException.getMessage();

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

        String expectedMessage = "The type of this file is not supported: " + htmlPath;

        assertThatThrownBy(() -> s3Adapter.loadFiles(filePaths))
                .isInstanceOf(FileTypeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

    @Test
    void testLoadFilesThrowsFileSizeValidationExceptionDueToInvalidBatchSize() throws Exception {
        String pathLargeFile = "path/to/largeFile";
        String pathSmallFile = "path/to/smallFile";
        List<String> filePaths = Arrays.asList(pathLargeFile, pathSmallFile);

        when(documentStorageFileRepository.getFileSize(eq(pathLargeFile))).thenReturn(ALLOWED_FILE_SIZE.toBytes());
        when(documentStorageFileRepository.getFileSize(eq(pathSmallFile))).thenReturn(
                DataSize.ofMegabytes(20).toBytes());

        DataSize sum = DataSize.ofBytes(ALLOWED_FILE_SIZE.toBytes() + DataSize.ofMegabytes(20).toBytes());
        String expectedMessage = String.format("Batch size of %d MB is too large. Allowed are %d MB.", sum.toMegabytes(), ALLOWED_BATCH_SIZE.toMegabytes());

        assertThatThrownBy(() -> s3Adapter.loadFiles(filePaths))
                .isInstanceOf(FileSizeValidationException.class)
                .extracting("message")
                .isEqualTo(expectedMessage);
    }

    @Test
    void testLoadFilesThrowsFileSizeValidationExceptionDueToFileExceedingMaxSize() throws Exception {
        String pathLargeFile = "path/to/largeFile";
        String pathSmallFile = "path/to/smallFile";
        List<String> filePaths = Arrays.asList(pathLargeFile, pathSmallFile);

        when(documentStorageFileRepository.getFileSize(eq(pathLargeFile))).thenReturn(TOO_LARGE_FILE_SIZE);
        when(documentStorageFileRepository.getFileSize(eq(pathSmallFile))).thenReturn(10_240L);

        String expectedMessage = String.format("The following files exceed the maximum size of %d MB:%n%s: %d MB", ALLOWED_FILE_SIZE.toMegabytes(),
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
        final byte[] testPdf = new ClassPathResource("files/test/test-pdf.pdf").getInputStream().readAllBytes();
        final Content pdfContent = new Content("pdf", "test-pdf", testPdf);
        final String fullPath = String.format("%s/%s", folderPathWithoutSlash, "test-pdf.pdf");
        final String fullPathWrong = String.format("%s//%s", folderPathWithoutSlash, "test-pdf.pdf");

        this.s3Adapter.transferContent(List.of(pdfContent), folderPathWithSlash);
        this.s3Adapter.transferContent(List.of(pdfContent), folderPathWithoutSlash);

        verify(documentStorageFileRepository, never()).saveFile(eq(fullPathWrong), any(), anyInt());
        verify(documentStorageFileRepository, times(2)).saveFile(eq(fullPath), any(), anyInt());
    }
}
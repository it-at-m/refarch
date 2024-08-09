package de.muenchen.refarch.integration.s3.adapter.in.rest;

import de.muenchen.refarch.integration.s3.domain.exception.FileExistenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionMapping {

    @ExceptionHandler
    public ResponseEntity<String> handleFileExistenceException(FileExistenceException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

}

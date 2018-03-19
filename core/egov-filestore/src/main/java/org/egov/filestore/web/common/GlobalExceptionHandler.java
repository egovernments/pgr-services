package org.egov.filestore.web.common;

import lombok.extern.slf4j.Slf4j;
import org.egov.filestore.domain.exception.ArtifactNotFoundException;
import org.egov.filestore.domain.exception.EmptyFileUploadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ArtifactNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleFileNotFoundException(Exception e) {
    	log.error(e.getMessage());
        return e.getMessage();
    }

	@ExceptionHandler(value = EmptyFileUploadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleEmptyFileUploadRequestException(Exception e) {
    	log.error(e.getMessage());
		return e.getMessage();
	}

}

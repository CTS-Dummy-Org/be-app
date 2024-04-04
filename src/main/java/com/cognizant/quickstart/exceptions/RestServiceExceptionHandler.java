package com.cognizant.quickstart.exceptions;

import static java.lang.String.format;

import com.cognizant.quickstart.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestServiceExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @ExceptionHandler({AccessDeniedException.class})
  protected ResponseEntity<Object> handleAccessDeniedException(Exception exception,
                                                               WebRequest request) {
    logger.error("Encountered AccessDeniedException ", exception);
    String errorMessage = format("Encountered exception %s", exception.getMessage());
    return handleExceptionInternal(exception,
        new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage), new HttpHeaders(),
        HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler({Exception.class})
  protected ResponseEntity<Object> handleApiException(Exception exception, WebRequest request) {
    logger.error("Encountered an unhandled exception", exception);
    String errorMessage = format("Encountered exception %s", exception.getMessage());
    return handleExceptionInternal(exception,
        new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage),
        new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }
}

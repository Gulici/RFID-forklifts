package kcz.rfid.backend.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ProblemDetail handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        return (ProblemDetail) e.getBody();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException e) {
        return (ProblemDetail) e.getBody();
    }

    @ExceptionHandler(CannotDeleteResourceException.class)
    public ProblemDetail handleCannotDeleteResourceException(CannotDeleteResourceException e) {
        return (ProblemDetail) e.getBody();
    }
}

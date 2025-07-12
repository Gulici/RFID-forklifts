package kcz.rfid.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class ResourceAlreadyExistsException extends ErrorResponseException {
    public ResourceAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, createProblemDetail(message), null);
    }

    private static ProblemDetail createProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Resource already exists");
        problemDetail.setDetail(message);
        return problemDetail;
    }
}

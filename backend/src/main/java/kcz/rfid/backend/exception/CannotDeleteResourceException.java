package kcz.rfid.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class CannotDeleteResourceException extends ErrorResponseException {
    public CannotDeleteResourceException(String message) {
        super(HttpStatus.CONFLICT, createProblemDetail(message), null);
    }

    private static ProblemDetail createProblemDetail(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Cannot delete resource");
        problemDetail.setDetail(message);
        return problemDetail;
    }
}

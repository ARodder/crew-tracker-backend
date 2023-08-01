package net.aroder.TripTracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception class for handling file not found errors.
 * Extends the RuntimeException class.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyFileNotFoundException extends RuntimeException {

    /**
     * Constructs a new MyFileNotFoundException object with the specified error message.
     *
     * @param message The error message associated with the exception.
     */
    public MyFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new MyFileNotFoundException object with the specified error message and cause.
     *
     * @param message The error message associated with the exception.
     * @param cause   The cause of the exception, usually another exception that triggered this one.
     */
    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
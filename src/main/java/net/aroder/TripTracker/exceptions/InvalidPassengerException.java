package net.aroder.TripTracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPassengerException extends Exception{

    /**
     * Constructs a new MyFileNotFoundException object with the specified error message.
     *
     * @param message The error message associated with the exception.
     */
    public InvalidPassengerException(String message) {
        super(message);
    }

}

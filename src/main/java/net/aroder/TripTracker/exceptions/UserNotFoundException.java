package net.aroder.TripTracker.exceptions;

import jakarta.persistence.EntityNotFoundException;

/**
 * Custom exception class for handling user not found errors.
 * Extends the EntityNotFoundException class.
 */
public class UserNotFoundException extends EntityNotFoundException {

    /**
     * Constructs a new UserNotFoundException object with a default error message.
     */
    public UserNotFoundException(String s) {
        super("User not found");
    }
}
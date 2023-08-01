package net.aroder.TripTracker.exceptions;

/**
 * Custom exception class for file storage-related errors.
 * Extends the RuntimeException class.
 */
public class FileStorageException extends RuntimeException {

    /**
     * Constructs a new FileStorageException object with the specified error message.
     *
     * @param message The error message associated with the exception.
     */
   public FileStorageException(String message) {
       super(message);
   }

    /**
     * Constructs a new FileStorageException object with the specified error message and cause.
     *
     * @param message The error message associated with the exception.
     * @param cause   The cause of the exception, usually another exception that triggered this one.
     */
   public FileStorageException(String message, Throwable cause) {
       super(message, cause);
   }
}

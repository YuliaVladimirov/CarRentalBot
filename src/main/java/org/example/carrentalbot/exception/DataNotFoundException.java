package org.example.carrentalbot.exception;

import lombok.Getter;

/**
 * Exception thrown when required data is not found in the expected source.
 * <p>This is typically used when a mandatory value is missing from session,
 * database, or any other persistence layer where the application expects
 * the data to exist.</p>
 */
@Getter
public class DataNotFoundException extends RuntimeException {

    /**
     * Creates a new exception with the specified error message.
     *
     * @param message detailed explanation of the missing data
     */
    public DataNotFoundException(String message) {
        super(message);
    }
}

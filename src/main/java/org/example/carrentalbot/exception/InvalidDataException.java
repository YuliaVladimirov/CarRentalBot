package org.example.carrentalbot.exception;

import lombok.Getter;

/**
 * Exception thrown when provided data is invalid or does not meet expected rules.
 * <p>This is typically used for validation failures such as incorrect formats,
 * out-of-range values, or inconsistent business rules.</p>
 */
@Getter
public class InvalidDataException extends RuntimeException {

    /**
     * Creates a new exception with the specified error message.
     *
     * @param message detailed explanation of why the data is invalid
     */
    public InvalidDataException(String message) {
        super(message);
    }

}
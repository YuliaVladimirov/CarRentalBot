package org.example.carrentalbot.exception;

import lombok.Getter;

/**
 * Exception thrown when the application reaches an unexpected or inconsistent state.
 * <p>This is typically used when the current execution context does not match any
 * valid business workflow or when required state transitions are missing.</p>
 */
@Getter
public class InvalidStateException  extends RuntimeException {


    /**
     * Creates a new exception with the specified error message.
     *
     * @param message detailed explanation of the invalid state
     */
    public InvalidStateException(String message) {
        super(message);
    }
}

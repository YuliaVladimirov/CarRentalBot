package org.example.carrentalbot.exception;

import lombok.Getter;

/**
 * Exception thrown when an operation is executed in an invalid or unsupported flow context.
 * <p>This is used to enforce workflow boundaries and prevent users from performing actions
 * outside the expected conversational state.</p>
 */
@Getter
public class InvalidFlowContextException  extends RuntimeException{

    public InvalidFlowContextException(String message) {
        super(message);
    }
}

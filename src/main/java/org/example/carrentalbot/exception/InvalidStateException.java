package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidStateException  extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}

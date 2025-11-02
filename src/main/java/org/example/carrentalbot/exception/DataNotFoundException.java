package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String message) {
        super(message);
    }
}

package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }

}
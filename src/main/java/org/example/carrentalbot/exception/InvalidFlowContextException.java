package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidFlowContextException  extends RuntimeException{

    public InvalidFlowContextException(String message) {
        super(message);
    }
}

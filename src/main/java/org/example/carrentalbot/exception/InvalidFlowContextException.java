package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidFlowContextException  extends RuntimeException{

    private final Long chatId;
    private final String message;

    public InvalidFlowContextException(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }
}

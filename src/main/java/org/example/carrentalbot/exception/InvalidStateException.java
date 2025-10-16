package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidStateException  extends RuntimeException {

    private final Long chatId;
    private final String message;

    public InvalidStateException(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }
}

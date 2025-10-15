package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class DataNotFoundException extends RuntimeException {
    private final Long chatId;
    private final String message;

    public DataNotFoundException(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }
}

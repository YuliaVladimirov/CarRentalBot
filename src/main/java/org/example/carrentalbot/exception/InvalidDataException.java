package org.example.carrentalbot.exception;

import lombok.Getter;

@Getter
public class InvalidDataException extends RuntimeException {

    private final Long chatId;
    private final String message;

    public InvalidDataException(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }

}
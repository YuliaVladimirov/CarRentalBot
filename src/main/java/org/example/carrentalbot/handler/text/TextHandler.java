package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.MessageDto;

public interface TextHandler {
    boolean canHandle(String text);
    void handle(Long chatId, MessageDto message);
}

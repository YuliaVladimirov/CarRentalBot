package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;

public interface CommandHandler {
    String getCommand();
    void handle(Long chatId, MessageDto message);
}

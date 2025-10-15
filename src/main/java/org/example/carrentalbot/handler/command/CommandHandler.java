package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

public interface CommandHandler {
    String getCommand();
    EnumSet<FlowContext> getAllowedContexts();
    void handle(Long chatId, MessageDto message);
}

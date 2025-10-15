package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

public interface TextHandler {
    boolean canHandle(String text);
    EnumSet<FlowContext> getAllowedContexts();
    void handle(Long chatId, MessageDto message);
}

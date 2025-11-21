package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

public interface TextHandler {
    boolean canHandle(String text);
    EnumSet<FlowContext> getAllowedContexts();
    void handle(Long chatId, String text);
}

package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.model.enums.FlowContext;

import java.util.EnumSet;

public interface CallbackHandler {
    String getKey();
    EnumSet<FlowContext> getAllowedContexts();
    void handle(Long chatId, CallbackQueryDto callbackQuery);
}

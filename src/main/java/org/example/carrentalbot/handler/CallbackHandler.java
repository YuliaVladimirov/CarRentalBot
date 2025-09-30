package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;

public interface CallbackHandler {
    String getKey();
    void handle(Long chatId, CallbackQueryDto callbackQuery);
}

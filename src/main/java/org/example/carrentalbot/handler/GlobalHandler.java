package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.UpdateDto;

public interface GlobalHandler {
    void handleUpdate(UpdateDto update);
    void handleMessage(MessageDto message);
    void handleCallbackQuery(CallbackQueryDto callbackQuery);
}

package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.springframework.stereotype.Component;

@Component
public class ConfirmBookingHandler implements CallbackHandler{

    public static final String KEY = "CONFIRM_BOOKING";

    @Override
    public String getKey() {
        return "";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

    }
}

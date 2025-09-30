package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class FallbackCallbackHandler implements CallbackHandler {

    private final TelegramClient telegramClient;

    public FallbackCallbackHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return "__FALLBACK__";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("⚠️ This button is not supported. Please use the main menu (/menu).")
                .parseMode("HTML")
                .build());
    }
}

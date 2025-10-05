package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class FallbackTextHandler implements TextHandler {

    private final TelegramClient telegramClient;

    public FallbackTextHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public boolean canHandle(String text) {
        return false;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("❓ I did not understand that. Please choose an option or type /help.")
                .parseMode("HTML")
                .build());
    }
}

package org.example.carrentalbot.handler.command;

import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class FallbackCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    public FallbackCommandHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "__FALLBACK__";
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("❓ Sorry, I did not understand that command. Type /help for available options.")
                .parseMode("HTML")
                .build());
    }
}

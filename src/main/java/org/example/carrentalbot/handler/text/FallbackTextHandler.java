package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class FallbackTextHandler implements TextHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    private final TelegramClient telegramClient;

    @Override
    public boolean canHandle(String text) {
        return false;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, String t) {

        String text = """
                ⚠️ Sorry, I did not understand that text.
                
                Please reenter or return to the main menu (or type /main)
                For other available options type /help.
                """;

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .build());
    }
}

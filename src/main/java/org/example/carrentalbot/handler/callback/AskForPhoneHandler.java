package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class AskForPhoneHandler implements CallbackHandler {

    public static final String KEY = "ASK_FOR_PHONE";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final NavigationService navigationService;
    private final TelegramClient telegramClient;

    public AskForPhoneHandler(NavigationService navigationService,
                              TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String text = """
                Please enter your your phone number.
                
                Example: +49 123 456789
                """;

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());

    }
}

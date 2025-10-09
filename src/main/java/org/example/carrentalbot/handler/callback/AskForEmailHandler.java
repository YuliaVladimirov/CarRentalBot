package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class AskForEmailHandler  implements CallbackHandler {


    public static final String KEY = "ASK_FOR_EMAIL";

    private final NavigationService navigationService;
    private final TelegramClient telegramClient;

    public AskForEmailHandler(NavigationService navigationService,
                              TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.telegramClient = telegramClient;
    }
    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String text = """
        Please enter your email address.

        Example: user@example.com
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

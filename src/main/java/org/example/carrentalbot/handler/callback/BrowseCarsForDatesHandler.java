package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class BrowseCarsForDatesHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CARS_FOR_DATES";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;

    public BrowseCarsForDatesHandler(NavigationService navigationService, SessionService sessionService,
                                     TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        sessionService.put(chatId, "carBrowsingMode", KEY);

        String text = """
        Please enter your rental period.
        Format: DD.MM.YYYY - DD.MM.YYYY
        Example: 05.10.2025 - 10.10.2025
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

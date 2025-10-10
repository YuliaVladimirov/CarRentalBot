package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class EditBookingDetailsHandler implements CallbackHandler {

    public static final String KEY = "EDIT_BOOKING_DETAILS";

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;
    private final NavigationService navigationService;

    public EditBookingDetailsHandler(
            TelegramClient telegramClient,
            KeyboardFactory keyboardFactory,
            NavigationService navigationService
    ) {
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
        this.navigationService = navigationService;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String text = """
                What would you like to edit?
                
                Please choose an option below:
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildEditBookingKeyboard();

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());

    }
}

package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BrowseCarsChoiceHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CARS_CHOICE";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseCarsChoiceHandler(NavigationService navigationService,
                                   SessionService sessionService,
                                   KeyboardFactory keyboardFactory,
                                   TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }


    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        if (sessionService.get(chatId, "category", CarCategory.class).isEmpty()) {
            CarCategory extractedCategory = extractCategoryFromCallback(chatId, callbackQuery.getData());
            sessionService.put(chatId, "category", extractedCategory);
        }

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarChoiceKeyboard();

        navigationService.push(chatId, KEY);

        SendMessageDto message = SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Choose browsing mode:")
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build();

        telegramClient.sendMessage(message);
    }

    private CarCategory extractCategoryFromCallback(Long chatId, String callbackData) {
        String categoryName = Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .orElseThrow(() ->
                        new InvalidDataException(chatId, "❌ Missing category information.")
                );

        try {
            return CarCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(chatId, String.format("❌ Invalid category: %s.", categoryName));
        }
    }
}

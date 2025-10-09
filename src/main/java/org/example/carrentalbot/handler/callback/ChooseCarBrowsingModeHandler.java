package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChooseCarBrowsingModeHandler implements CallbackHandler {

    public static final String KEY = "CHOOSE_CAR_BROWSING_MODE";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public ChooseCarBrowsingModeHandler(NavigationService navigationService,
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

        handleCategory(chatId, callbackQuery.getData());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarBrowsingModeKeyboard();

        navigationService.push(chatId, KEY);

        SendMessageDto message = SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Choose browsing mode:</b>")
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build();

        telegramClient.sendMessage(message);
    }

    private void handleCategory(Long chatId, String callbackData) {

        CarCategory fromCallback = extractCategoryFromCallback(chatId, callbackData);
        CarCategory fromSession = sessionService.get(chatId, "carCategory", CarCategory.class).orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException(chatId, "❌ Car category not found in callback or session");
        }

        CarCategory result = fromCallback != null ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carCategory", result);
        }

    }

    private CarCategory extractCategoryFromCallback(Long chatId, String callbackData) {
        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(String::toUpperCase)
                .map(categoryStr -> {
                    try {
                        return CarCategory.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException(chatId, "❌ Invalid category: " + categoryStr);
                    }
                })
                .orElse(null);
    }
}

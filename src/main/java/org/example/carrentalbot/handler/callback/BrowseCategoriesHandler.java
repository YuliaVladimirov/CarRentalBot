package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrowseCategoriesHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CATEGORIES";

    private final CarService carService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseCategoriesHandler(CarService carService,
                                   NavigationService navigationService,
                                   KeyboardFactory keyboardFactory,
                                   TelegramClient telegramClient) {
        this.carService = carService;
        this.navigationService = navigationService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        List<CarProjectionDto> carCategories = carService.getCarCategories();
        InlineKeyboardMarkupDto keyboard = keyboardFactory.buildCarCategoryKeyboard(carCategories);

        navigationService.push(chatId, KEY);
        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Available Categories")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
    }
}

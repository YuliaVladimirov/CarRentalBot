package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.CategoryAvailabilityDto;
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
        return "BROWSE_CATEGORIES";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto query) {
        List<CategoryAvailabilityDto> availableCars = carService.getAvailableCarCounts();
        InlineKeyboardMarkupDto keyboard = keyboardFactory.buildCarCategoryKeyboard(availableCars);

        navigationService.push(chatId, "BROWSE_CATEGORIES");
        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Available Cars By Category")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
    }
}

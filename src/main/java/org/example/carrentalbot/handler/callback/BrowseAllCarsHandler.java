package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrowseAllCarsHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_ALL_CARS";

    private final CarService carService;
    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseAllCarsHandler(CarService carService,
                                NavigationService navigationService, SessionService sessionService,
                                KeyboardFactory keyboardFactory,
                                TelegramClient telegramClient) {
        this.carService = carService;
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

        CarCategory carCategory = sessionService
                .get(chatId, "category", CarCategory.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Category not found"));

        List<Car> allCars = carService.getAllCarsByCategory(carCategory);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarKeyboard(allCars);

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(String.format("Available cars in %s category:", carCategory))
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

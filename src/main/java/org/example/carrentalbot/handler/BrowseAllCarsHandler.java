package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrowseAllCarsHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_ALL_CARS";

    private final CarService carService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseAllCarsHandler(CarService carService,
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

        String categoryName;
        if (callbackQuery.getData().contains(":")) {
            categoryName = callbackQuery.getData().split(":", 2)[1];
        } else {
            categoryName = null;
        }

        CarCategory carCategory;
        try {
            assert categoryName != null;
            carCategory = CarCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(chatId, String.format("❌ Invalid category: %s.", categoryName));
        }

        List<Car> cars = carService.getCarsByCategory(carCategory);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarKeyboard(cars);

        navigationService.push(chatId, KEY);
        SendMessageDto message = SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(String.format("Available cars in %s category", categoryName))
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build();

        telegramClient.sendMessage(message);
    }
}

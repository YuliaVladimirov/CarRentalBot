package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CarDetailsHandler implements CallbackHandler {

    private final CarService carService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public CarDetailsHandler(CarService carService,
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
        return "CAR_DETAILS:";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String carIdString;
        UUID carId;
        if (callbackQuery.getData().contains(":")) {
            carIdString = callbackQuery.getData().split(":", 2)[1];
        } else {
            carIdString = null;
        }

        try {
            assert carIdString != null;
            carId = UUID.fromString(carIdString);

        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(chatId, "Invalid UUID format: " + carIdString);
        }

        Car car = carService.getCarInfo(carId).orElseThrow(
                () -> new DataNotFoundException(chatId, String.format("User with id: %s, was not found.", carIdString)));

        navigationService.push(chatId, "CAR_DETAILS:");
        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarDetailsKeyboard(car);
        String text = String.format("""
                🚘 Car Details:
                
                🏷️ Brand: %s
                📌 Model: %s
                📝 Description: %s
                💰 Daily Rate: €%s
                🖼️ Image: <a href="%s">View Image</a>
                """, car.getBrand(), car.getModel(), car.getDescription(), car.getDailyRate(), car.getImageUrl());

        SendMessageDto message = SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build();

        telegramClient.sendMessage(message);
    }
}

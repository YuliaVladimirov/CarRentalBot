package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Component
public class DisplayCarDetailsHandler implements CallbackHandler {

    public static final String KEY = "DISPLAY_CAR_DETAILS";

    private final CarService carService;
    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public DisplayCarDetailsHandler(CarService carService,
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

        UUID carId = sessionService.get(chatId, "carId", UUID.class)
                .orElseGet(() -> {
                    UUID extractCarIdFromCallback = extractCarIdFromCallback(chatId, callbackQuery.getData());
                    sessionService.put(chatId, "carId", extractCarIdFromCallback);
                    return extractCarIdFromCallback;
                });

        Car car = carService.getCarInfo(carId).orElseThrow(
                () -> new DataNotFoundException(chatId, String.format("User with id: %s, was not found.", carId)));

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarDetailsKeyboard(car);
        String text = String.format("""
                🚘 Car Details:
                
                🏷️ Brand: %s
                📌 Model: %s
                📝 Description: %s
                💰 Daily Rate: €%s/day
                """, car.getBrand(), car.getModel(), car.getDescription(), car.getDailyRate().setScale(0, RoundingMode.HALF_UP));

        navigationService.push(chatId, KEY);

        telegramClient.sendPhoto(SendPhotoDto.builder()
                .chatId(chatId.toString())
                .photo(car.getImageFileId())
                .caption(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private UUID extractCarIdFromCallback (Long chatId, String callbackData) {

        String carIdString = Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .orElseThrow(() ->
                        new InvalidDataException(chatId, "❌ Missing car id information.")
                );

        try {
            return UUID.fromString(carIdString);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(chatId, String.format("Invalid UUID format: %s",carIdString));
        }
    }
}

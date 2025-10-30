package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
public class BrowseAllCarsHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_ALL_CARS";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final CarService carService;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseAllCarsHandler(CarService carService,
                                SessionService sessionService,
                                NavigationService navigationService,
                                KeyboardFactory keyboardFactory,
                                TelegramClient telegramClient) {
        this.carService = carService;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        updateBrowsingModeInSession(chatId, callbackQuery.getData());

        CarCategory carCategory = sessionService
                .getCarCategory(chatId, "carCategory")
                .orElseThrow(() -> new DataNotFoundException(chatId, "Category not found"));

        List<Car> allCars = carService.getAllCarsByCategory(carCategory);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarsKeyboard(allCars);

        String text = String.format("""
                        <b>All cars in category '%s':</b>
                        """, carCategory.getValue());

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private void updateBrowsingModeInSession(Long chatId, String callbackData) {
        CarBrowsingMode fromCallback = extractBrowsingModeFromCallback(chatId, callbackData);

        CarBrowsingMode fromSession = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException(chatId, "Car browsing mode not found in callback or session");
        }

        CarBrowsingMode result = fromCallback != null ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carBrowsingMode", result);
        }
    }

    private CarBrowsingMode extractBrowsingModeFromCallback(Long chatId, String callbackData) {
        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(String::toUpperCase)
                .map(categoryStr -> {
                    try {
                        return CarBrowsingMode.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException(chatId, "Invalid car browsing mode: " + categoryStr);
                    }
                })
                .orElse(null);
    }
}

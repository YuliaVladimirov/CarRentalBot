package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class DisplayCarDetailsHandler implements CallbackHandler {

    public static final String KEY = "DISPLAY_CAR_DETAILS";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

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
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        UUID carId = retrieveCarId(chatId, callbackQuery.getData());
        Car car = carService.getCar(chatId, carId);

        Map.Entry<String, String> data = getDataForKeyboard(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarDetailsKeyboard(data.getKey(), data.getValue());

        String text = String.format("""
                🚘  <b>Car Details</b>:
                
                🏷️  Brand:  %s
                📌  Model:  %s
                📝  Description:  %s
                💰  Daily Rate:  €%s/day
                """, car.getBrand(), car.getModel(), car.getDescription(), car.getDailyRate().setScale(0, RoundingMode.HALF_UP));

        navigationService.push(chatId, KEY + ":" + car.getId());

        telegramClient.sendPhoto(SendPhotoDto.builder()
                .chatId(chatId.toString())
                .photo(car.getImageFileId())
                .caption(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private UUID retrieveCarId(Long chatId, String callbackData) {

        UUID fromCallback = extractCarIdFromCallback(chatId, callbackData);
        UUID fromSession = sessionService.get(chatId, "carId", UUID.class).orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException(chatId, "❌ Car id not found in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carId", result);
        }

        return result;
    }

    private UUID extractCarIdFromCallback (Long chatId, String callbackData) {

        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(idStr -> {
                    try {
                        return UUID.fromString(idStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException(chatId, "❌ Invalid UUID format: " + idStr);
                    }
                })
                .orElse(null);
    }

    private Map.Entry<String, String> getDataForKeyboard(Long chatId) {
        CarBrowsingMode carBrowsingMode = sessionService.get(chatId, "carBrowsingMode", CarBrowsingMode.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Car browsing mode not found"));

        return switch (carBrowsingMode) {
            case ALL_CARS -> Map.entry(AskForRentalDatesHandler.KEY, "🕒 Check Availability");
            case CARS_FOR_DATES -> Map.entry(AskForPhoneHandler.KEY, "📝 Start Booking");
        };
    }
}

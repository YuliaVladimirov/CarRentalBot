package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DisplayBookingDetailsHandler implements CallbackHandler{

public static final String KEY = "DISPLAY_BOOKING_DETAILS";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;
    private final CarService carService;
    private final KeyboardFactory keyboardFactory;

    public DisplayBookingDetailsHandler(NavigationService navigationService, SessionService sessionService,
                                        TelegramClient telegramClient, CarService carService, KeyboardFactory keyboardFactory) {
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.telegramClient = telegramClient;
        this.carService = carService;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        UUID carId = sessionService.get(chatId, "carId", UUID.class).orElseThrow(() -> new DataNotFoundException(chatId, "Car id not found"));

        Car car = carService.getCarInfo(carId).orElseThrow(() -> new DataNotFoundException(chatId, String.format("Car with id: %s, was not found.", carId)));

        LocalDate startDate = sessionService.get(chatId, "startDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "Start date not found"));
        LocalDate endDate = sessionService.get(chatId, "endDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "End date not found"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        BigDecimal dailyRate = car.getDailyRate().setScale(0, RoundingMode.HALF_UP);

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal totalCost = car.getDailyRate().multiply(BigDecimal.valueOf(totalDays));

        String phone = sessionService.get(chatId, "phone", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Phone not found"));

        String email = sessionService.get(chatId, "email", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Email not found"));

        String text = String.format("""
                <b>Your booking details:</b>
                
                🚗  Car:  %s (%s)
                       Category:  %s
                📅  Rental period:  %s - %s
                       Total Days:  %d
                💰  Daily Rate:  €%s/day
                       Total Cost:  €%s
                
                📞  Phone number:  %s
                📧  Email:  %s
                """,
                car.getBrand(), car.getModel(), car.getCategory().getValue(),
                startDate.format(formatter), endDate.format(formatter),
                totalDays,
                dailyRate, totalCost,
                phone, email);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildBookingDetailsKeyboard();

        navigationService.push(chatId, KEY);

        telegramClient.sendPhoto(SendPhotoDto.builder()
                .chatId(chatId.toString())
                .photo(car.getImageFileId())
                .caption(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

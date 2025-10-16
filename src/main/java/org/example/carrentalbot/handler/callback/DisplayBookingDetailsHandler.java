package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
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
import java.util.EnumSet;
import java.util.UUID;

@Component
public class DisplayBookingDetailsHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);
    public static final String KEY = "DISPLAY_BOOKING_DETAILS";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final CarService carService;
    private final BookingService bookingService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public DisplayBookingDetailsHandler(NavigationService navigationService,
                                        SessionService sessionService,
                                        CarService carService, BookingService bookingService, KeyboardFactory keyboardFactory,
                                        TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.carService = carService;
        this.bookingService = bookingService;
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

        sessionService.put(chatId, "flowContext", FlowContext.BOOKING_FLOW);

        UUID carId = sessionService.get(chatId, "carId", UUID.class).orElseThrow(() -> new DataNotFoundException(chatId, "Car id not found"));

        LocalDate startDate = sessionService.get(chatId, "startDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "Start date not found"));

        LocalDate endDate = sessionService.get(chatId, "endDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "End date not found"));

        String phone = sessionService.get(chatId, "phone", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Phone not found"));

        String email = sessionService.get(chatId, "email", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Email not found"));

        Car car = carService.getCar(chatId, carId);

        BigDecimal dailyRate = car.getDailyRate().setScale(0, RoundingMode.HALF_UP);

        long totalDays = bookingService.calculateTotalDays(startDate, endDate);
        BigDecimal totalCost = bookingService.calculateTotalCost(dailyRate, totalDays);
        sessionService.put(chatId, "totalCost", totalCost);

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
                startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
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

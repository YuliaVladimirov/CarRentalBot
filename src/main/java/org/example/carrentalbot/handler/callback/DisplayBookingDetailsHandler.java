package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.*;
import org.example.carrentalbot.session.SessionService;
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
@RequiredArgsConstructor
public class DisplayBookingDetailsHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);
    public static final String KEY = "DISPLAY_BOOKING_DETAILS";

    private final SessionService sessionService;
    private final CarService carService;
    private final BookingService bookingService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

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

        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));

        String phone = sessionService
                .getString(chatId, "phone")
                .orElseThrow(() -> new DataNotFoundException("Phone not found in session"));

        String email = sessionService
                .getString(chatId, "email")
                .orElseThrow(() -> new DataNotFoundException("Email not found in session"));

        Car car = carService.getCar(carId);

        BigDecimal dailyRate = car.getDailyRate().setScale(0, RoundingMode.HALF_UP);

        Integer totalDays = bookingService.calculateTotalDays(startDate, endDate);
        sessionService.put(chatId, "totalDays", totalDays);

        BigDecimal totalCost = bookingService.calculateTotalCost(dailyRate, totalDays);
        sessionService.put(chatId, "totalCost", totalCost);

        String text = String.format("""
                        <b>Your booking details:</b>
                       
                       🚗  Car:  %s (%s)
                       🏷️  Category:  %s
                       
                       📅  Rental period:  %s - %s
                       📆  Total Days: %d
                       💰  Daily Rate:  €%s/day
                       💳  Total Cost:  €%s
                       
                       📞  Phone number:  %s
                       📧  Email:  %s
                       """,
                car.getBrand(), car.getModel(), car.getCategory().getValue(),
                startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                totalDays,
                dailyRate, totalCost,
                phone, email);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildBookingDetailsKeyboard();

        telegramClient.sendPhoto(SendPhotoDto.builder()
                .chatId(chatId.toString())
                .photo(car.getImageFileId())
                .caption(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Callback handler responsible for displaying the final booking summary.
 *
 * <p>Operates within booking-related flows and presents aggregated booking data,
 * including car details, dates, and pricing, before confirmation.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayBookingDetailsHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "DISPLAY_BOOKING_DETAILS";

    /**
     * Allowed flow contexts for this handler.
     * Handler is available during booking and booking-editing flows.
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Service for retrieving car data used in booking and display flows.
     */
    private final CarService carService;

    /**
     * Service for booking logic and availability checks.
     */
    private final BookingService bookingService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building inline keyboards for booking interactions.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Builds and displays the final booking details for confirmation.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if required session data is missing
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'display booking details' flow");

        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));
        log.debug("Loaded from session: carId={}", carId);

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));
        log.debug("Loaded from session: startDate={}", startDate);

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));
        log.debug("Loaded from session: endDate={}", endDate);

        String phone = sessionService
                .getString(chatId, "phone")
                .orElseThrow(() -> new DataNotFoundException("Phone not found in session"));
        log.debug("Loaded from session: phone={}", phone);

        String email = sessionService
                .getString(chatId, "email")
                .orElseThrow(() -> new DataNotFoundException("Email not found in session"));
        log.debug("Loaded from session: email={}", email);

        Car car = carService.getCar(carId);

        BigDecimal dailyRate = car.getDailyRate().setScale(0, RoundingMode.HALF_UP);

        Integer totalDays = bookingService.calculateTotalDays(startDate, endDate);
        sessionService.put(chatId, "totalDays", totalDays);
        log.debug("Session updated: 'totalDays' set to {}", totalDays);

        BigDecimal totalCost = bookingService.calculateTotalCost(dailyRate, totalDays);
        sessionService.put(chatId, "totalCost", totalCost);
        log.debug("Session updated: 'totalCost' set to {}", totalCost);

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

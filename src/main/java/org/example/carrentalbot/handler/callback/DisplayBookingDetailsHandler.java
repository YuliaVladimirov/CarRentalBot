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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service serves as the final confirmation screen in the booking lifecycle.
 * It aggregates all transient session data and car information to present a comprehensive
 * summary to the user. Its responsibilities include:
 * <ul>
 * <li>Providing the unique {@code DisplayBookingDetailsHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Retrieving vehicle details, selected dates, and contact information from the session.</li>
 * <li>Invoking {@link BookingService} to calculate the total rental duration and final price.</li>
 * <li>Formatting the data into a high-readability HTML summary.</li>
 * <li>Dispatching a rich media message (photo of the car + details) with the final action keyboard.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayBookingDetailsHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code DisplayBookingDetailsHandler} and properly route callbacks.
     */
    public static final String KEY = "DISPLAY_BOOKING_DETAILS";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BOOKING_FLOW} and
     * {@link FlowContext#EDIT_BOOKING_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Service responsible for retrieving the full {@link Car} entity, including technical
     * specifications and the {@code imageFileId} for the summary photo.
     */
    private final CarService carService;

    /**
     * Service responsible for performing business logic calculations, specifically for determining
     * the rental duration and applying the daily rate to compute the total cost.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for managing user-specific session data, specifically
     * the final calculated totals for the booking.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for generating the final action keyboard, specifically
     * offering "Confirm", "Edit" and "Cancel" options.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically for sending the vehicle photo with the summarized booking details as a caption.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@link #KEY}.
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * Returns the allowed contexts for this handler.
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Orchestrates the construction and display of the final booking summary.
     * <ol>
     * <li>Fetches the following from {@link SessionService}:
     * <ul>
     * <li>{@code carId} (UUID)</li>
     * <li>{@code startDate} & {@code endDate} (in {@link LocalDate} format)</li>
     * <li>{@code phone} & {@code email} (in {@link String} format)</li>
     * </ul>
     * <li>Calculates the total days and total cost
     * </li>
     * <li>Updates the session with these calculated values for use in the final persistence step.</li>
     * <li>Constructs a stylized HTML caption featuring car specifications and pricing.</li>
     * <li>Dispatches the message with car's image and final booking summary using {@code sendPhoto}.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if any required piece of the booking puzzle is missing from the session.
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

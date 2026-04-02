package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Callback handler responsible for displaying detailed booking information.
 * <p>Shows a full booking summary for a selected reservation in the "My Bookings" flow.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayMyBookingDetailsHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "MY_BOOKING_DETAILS";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the "My Bookings" flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service for retrieving booking data.
     */
    private final BookingService bookingService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building booking detail keyboards.
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
     * Loads and displays detailed information for a selected booking.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing booking reference
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'display my booking details' flow");

        UUID bookingId = updateBookingIdInSession(chatId, callbackQuery.getData());

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        String text = String.format("""
                        <b>Booking details:</b>
                        
                        🆔  Booking id:  %s
                        🚗  Car:  %s (%s)
                        🏷️  Category:  %s
                        
                        📅  Rental period:  %s - %s
                        📆  Total Days: %d
                        💰  Daily Rate:  €%s/day
                        💳  Total Cost:  €%s
                        
                        📞  Phone number:  %s
                        📧  Email:  %s
                        
                        📦  Status: %s
                        """,
                booking.getId(),
                booking.getCar().getBrand(),
                booking.getCar().getModel(),
                booking.getCar().getCategory().getValue(),
                booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                booking.getTotalDays(),
                booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP),
                booking.getTotalCost(),
                booking.getPhone(),
                booking.getEmail(),
                booking.getStatus());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMyBookingDetailsKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    /**
     * Resolves booking id from callback or session and synchronizes session state.
     *
     * @return resolved car identifier
     * @throws DataNotFoundException if no booking ID can be resolved from callback or session
     */
    private UUID updateBookingIdInSession(Long chatId, String callbackData) {
        UUID fromCallback = extractBookingIdFromCallback(callbackData);
        log.debug("Extracted from callback: booking id={}", fromCallback);

        UUID fromSession = sessionService
                .getUUID(chatId, "bookingId")
                .orElse(null);
        log.debug("Loaded from session: bookingId={}", fromSession);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Missing booking id in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "bookingId", result);
            log.debug("Session updated: 'bookingId' set to {}", result);
        } else {
            log.debug("Session unchanged: 'bookingId' remains {}", result);
        }

        return result;
    }

    /**
     * Extracts a {@link UUID} from callback data.
     *
     * @param callbackData raw callback payload
     * @return parsed UUID or {@code null} if absent
     * @throws InvalidDataException if the value is invalid
     */
    private UUID extractBookingIdFromCallback(String callbackData) {

        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(idStr -> {
                    try {
                        return UUID.fromString(idStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException("Invalid UUID format: " + idStr);
                    }
                })
                .orElse(null);
    }
}

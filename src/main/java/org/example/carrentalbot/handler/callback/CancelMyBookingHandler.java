package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Callback handler responsible for managing booking cancellation from "My Bookings".
 * <p>Validates whether a booking can be canceled and, if eligible, requests user confirmation.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelMyBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CANCEL_MY_BOOKING";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the "My Bookings" flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service for retrieving and managing booking data.
     */
    private final BookingService bookingService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building booking-related keyboards.
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
     * Validates cancellation rules and either rejects, informs, or requests confirmation.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if booking id is missing from session
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'cancel my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        LocalDate today = LocalDate.now();

        String text;
        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            text = """
                    ⚠️ This booking has already been canceled.
                    
                    You can return to the main menu.
                    """;
        } else if (today.isEqual(booking.getStartDate()) || today.isAfter(booking.getStartDate())) {
            text = """
                    ⚠️ This booking can no longer be canceled.
                    
                    Cancellations are allowed
                    up to 1 day before the rental start date.
                    """;
        } else {
            text = "Are you sure you want to cancel this booking?";

            replyMarkup = keyboardFactory.buildCancelMyBookingKeyboard();
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build());
    }
}

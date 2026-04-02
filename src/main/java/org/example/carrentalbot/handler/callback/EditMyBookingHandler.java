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

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Callback handler responsible for editing an existing booking from "My Bookings".
 * <p>Validates whether a booking can be modified and allows editing of contact information.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditMyBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "EDIT_MY_BOOKING";

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
     * Clock used for time-based validation to allow testability and consistent time access.
     */
    private final Clock clock;

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
     * Validates whether a booking can be edited and either denies or starts the edit flow.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if booking id is missing from session
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'edit my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        LocalDate today = LocalDate.now(clock);

        String text;
        InlineKeyboardMarkupDto replyMarkup;

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            text = """
                    ⚠️ This booking has already been canceled
                    and cannot be edited.
                    
                    You can make a new booking from the main menu.
                    """;
            replyMarkup = keyboardFactory.buildToMainMenuKeyboard();
        } else if (!today.isBefore(booking.getStartDate())) {
            text = """
                    ⚠️ This booking can no longer be edited.
                    Changes can be made up to one day
                    before the rental start date.
                    
                    You can make a new booking from the main menu.
                    """;
            replyMarkup = keyboardFactory.buildToMainMenuKeyboard();
        } else {

            text = """
                    ⚠️ <i>To change your rental dates,</i>
                    <i>please cancel your current booking</i>
                    <i> and create a new one.</i>
                    
                    Update your contact info,
                    then press <b>Continue</b> when done.
                    """;

            replyMarkup = keyboardFactory.buildEditBookingKeyboard(ConfirmMyBookingHandler.KEY);
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

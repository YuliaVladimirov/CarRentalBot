package org.example.carrentalbot.handler.callback;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.email.EmailService;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.NotificationType;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.reminder.ReminderService;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Callback handler responsible for confirming cancellation of an existing booking.
 * <p>Cancels the booking, triggers notifications, and clears session state.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmCancelMyBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CONFIRM_CANCEL_MY_BOOKING";

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
     * Service for sending booking notifications.
     */
    private final EmailService emailService;

    /**
     * Service for managing booking reminders.
     */
    private final ReminderService reminderService;

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
     * Cancels the booking, notifies the user, and clears the session.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if booking id is missing from session
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm cancel my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.cancelBooking(bookingId);
        log.info("Booking canceled: id={}", booking.getId());

        reminderService.cancelReminders(booking);
        log.info("Reminders canceled for booking: booking id={}", booking.getId());

        String text = String.format("""
                ✅ Booking successfully canceled.
                Booking id: %s
                
                A confirmation of your booking cancellation
                has been sent to your email address: <b>%s</b>
                
                You can start a new booking anytime from the main menu.
                """, booking.getId(), booking.getEmail());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        sessionService.deleteAll(chatId);
        log.debug("Session cleared: chat id={}", chatId);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
        try {
            emailService.sendBookingNotification(booking, NotificationType.CANCELLATION);
        } catch (MessagingException exception) {
            log.error("CRITICAL: Failed to initiate email send for booking {}.", booking.getId(), exception);
        }
    }
}

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
import org.example.carrentalbot.model.Reminder;
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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service executes the final termination of an existing reservation.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmCancelMyBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Updating the {@link Booking} status from "Confirmed" to "Canceled" in the database.</li>
 * <li>Revoking all pending {@link Reminder} entries associated with the booking.</li>
 * <li>Dispatching a {@link NotificationType#CANCELLATION} email.</li>
 * <li>Clearing the user's conversational session.</li>
 * <li>Sends a Telegram message with the cancellation receipt.</li>
 * <li>Providing a clean transition back to the Main Menu.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmCancelMyBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ConfirmCancelMyBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CONFIRM_CANCEL_MY_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#MY_BOOKINGS_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for updating the {@link Booking} status to {@code CANCELLED}.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for dispatching the cancellation confirmation email.
     */
    private final EmailService emailService;

    /**
     * Service responsible for deleting or deactivate any scheduled reminders for the
     * now-canceled reservation.
     */
    private final ReminderService reminderService;

    /**
     * Service responsible for performing a full session purge upon successful cancellation.
     */
    private final SessionService sessionService;

    /**
     * Factory component responsible for generating the "To Main Menu" keyboard for the final exit.
     */
    private final KeyboardFactory keyboardFactory;
    /**
     * Component responsible for interacting with the Telegram Bot API
     * to deliver the final cancellation acknowledgment message
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
     * {@inheritDoc}
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Orchestrates the cancellation and cleanup process.
     * <ol>
     * <li>Retrieves the target {@code bookingId} from the {@link SessionService}.</li>
     * <li>Invokes {@link BookingService#cancelBooking(UUID)} to change the booking status in the database.</li>
     * <li>Calls {@link ReminderService#cancelReminders(Booking)} to prevent orphaned notifications from being sent to the user.</li>
     * <li>Sends a final acknowledgment message to the user confirming the action.</li>
     * <li>Purges all transient data in the session via {@code deleteAll}.</li>
     * <li>Attempts to send the final cancellation notification email.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the {@code bookingId} is missing from the session.
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

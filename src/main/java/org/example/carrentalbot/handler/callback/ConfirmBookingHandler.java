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
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Callback handler responsible for finalizing the booking process.
 *
 * <p>Persists the booking, triggers post-booking actions (reminders and email),
 * clears session state, and returns a final confirmation to the user.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CONFIRM_BOOKING";

    /**
     * Allowed flow contexts for this handler.
     * Handler is available during booking and booking-editing flows.
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Service for booking creation and persistence.
     */
    private final BookingService bookingService;

    /**
     * Service for sending booking confirmation emails to users.
     */
    private final EmailService emailService;

    /**
     * Service for scheduling booking reminders before rental start.
     */
    private final ReminderService reminderService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building post-booking navigation keyboards.
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
     * Finalizes the booking by persisting data and triggering post-booking actions.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if required session data is missing
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm booking' flow");

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

        Integer totalDays = sessionService
                .getInteger(chatId, "totalDays")
                .orElseThrow(() -> new DataNotFoundException("Total days not found in session"));
        log.debug("Loaded from session: totalDays={}", totalDays);

        BigDecimal totalCost = sessionService
                .getBigDecimal(chatId, "totalCost")
                .orElseThrow(() -> new DataNotFoundException("Total cost not found in session"));
        log.debug("Loaded from session: totalCost={}", totalCost);

        String phone = sessionService
                .getString(chatId, "phone")
                .orElseThrow(() -> new DataNotFoundException("Phone not found in session"));
        log.debug("Loaded from session: phone={}", phone);

        String email = sessionService
                .getString(chatId, "email")
                .orElseThrow(() -> new DataNotFoundException("Email not found in session"));
        log.debug("Loaded from session: email={}", email);

        Booking booking = bookingService.createBooking(carId, callbackQuery.getFrom().getId(),
                startDate, endDate, totalDays, totalCost,
                phone, email);
        log.info("New booking created: bookingId={}", booking.getId());

        List<Reminder> reminders = reminderService.createReminders(booking);
        log.info("Created {} reminders for booking: booking id={}", reminders.size(), booking.getId());

        String text = String.format("""
                Your booking has been <b>successfully confirmed</b>.
                Booking Id: <b>%s</b>
                
                The full booking info
                has been sent to your email address: <b>%s</b>
                
                Thank you for choosing our service! 🚗
                """, booking.getId(), booking.getEmail());

        sessionService.deleteAll(chatId);
        log.debug("Session cleared: chat id={}", chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());

        try (MDC.MDCCloseable ignoredReminderId = MDC.putCloseable("notificationType", NotificationType.CONFIRMATION.name())) {
            emailService.sendBookingNotification(booking, NotificationType.CONFIRMATION);
        } catch (MessagingException exception) {
            log.error("CRITICAL: Failed to initiate email send for booking {}.", booking.getId(), exception);
        }
    }
}
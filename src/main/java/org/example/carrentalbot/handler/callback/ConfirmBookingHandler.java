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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service marks the completion of the booking journey by persisting the
 * reservation and initiating post-booking processes. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Final validation and retrieval of the complete booking dataset from the session.</li>
 * <li>Persisting the new {@link Booking} entity via the {@link BookingService}.</li>
 * <li>Scheduling automated rental reminders through the {@link ReminderService}.</li>
 * <li>Triggering asynchronous email confirmations via the {@link EmailService}.</li>
 * <li>Clearing the user session to prevent data leakage and reset the bot state.</li>
 * <li>Dispatching an HTML-formatted message with a final confirmation receipt and a path back to the main menu.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ConfirmBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CONFIRM_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BOOKING_FLOW} and
     * {@link FlowContext#EDIT_BOOKING_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Service responsible for performing business logic calculations, specifically
     * for saving the {@link Booking} to the database.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for dispatching HTML-formatted email confirmations
     * to the user's provided email address.
     */
    private final EmailService emailService;

    /**
     * Service responsible for scheduling "push" notifications for the user as the
     * rental start date approaches.
     */
    private final ReminderService reminderService;

    /**
     * Service responsible for managing user-specific session data, specifically
     * to retrieve the final booking parameters and
     * perform a total session cleanup upon successful persistence.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for generating the final action keyboard, specifically
     * for generating the "To Main Menu" keyboard for post-flow navigation.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically for sending the final success message and booking ID to the user.
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
     * Orchestrates the finalization of the booking process.
     * <ol>
     * <li>Collects all seven mandatory data points from the {@link SessionService}.</li>
     * <li><b>Persistence:</b> Creates the {@link Booking} record in the database.</li>
     * <li><b>Automation:</b> Generates and schedules {@link Reminder} entries.</li>
     * <li><b>User Feedback:</b> Sends a Telegram message containing the new Booking ID.</li>
     * <li><b>Cleanup:</b> Purges all transient data from the session via {@code deleteAll}.</li>
     * <li><b>Notification:</b> Attempts to send an email confirmation (logged as critical failure if unsuccessful).</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The callback query containing the user's Telegram ID.
     * @throws DataNotFoundException if the session state is incomplete at the moment of confirmation.
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
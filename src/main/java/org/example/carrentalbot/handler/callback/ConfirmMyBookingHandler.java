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
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service finalizes the modification flow for an existing booking.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmMyBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Retrieving the targeted {@code bookingId} and the updated contact fields from the session.</li>
 * <li>Persisting changes to the {@link Booking} entity via the {@link BookingService}.</li>
 * <li>Notifying the user of the successful update via a detailed Telegram receipt.</li>
 * <li>Triggering an asynchronous email notification regarding the modification.</li>
 * <li>Performing a full session cleanup to reset the user's state.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmMyBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ConfirmMyBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CONFIRM_MY_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#MY_BOOKINGS_FLOW} as it concludes an
     * explicit management action.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for persisting the updated booking details to the database.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for dispatching {@link NotificationType#UPDATE} emails
     * to the user's email address.
     */
    private final EmailService emailService;

    /**
     * Service responsible for fetching the current update data and perform a total session
     * purge upon successful persistence.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for generating the "To Main Menu" keyboard for post-update navigation.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * confirmation summary.
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
     * Orchestrates the persistence of booking modifications and user notification.
     * <ol>
     * <li><b>Data Retrieval:</b> Fetches the {@code bookingId}, {@code phone}, and {@code email}
     * from the {@link SessionService}.</li>
     * <li><b>Persistence:</b> Invokes {@code bookingService.updateBooking} to commit the
     * changes to the database.</li>
     * <li><b>User Feedback:</b> Formats a comprehensive HTML message showing the
     * current state of the entire booking.</li>
     * <li><b>Cleanup:</b> Purges the user session via {@code deleteAll} to prevent
     * stale data from interfering with future interactions.</li>
     * <li><b>Notification:</b> Dispatches an update email; logs a critical error
     * if the email provider fails.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the {@code bookingId} is missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in message or session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        String phone = sessionService
                .getString(chatId, "phone")
                .orElse(null);
        log.debug("Loaded from session: phone={}", phone);

        String email = sessionService
                .getString(chatId, "email")
                .orElse(null);
        log.debug("Loaded from session: email={}", email);

        Booking booking = bookingService.updateBooking(bookingId, phone, email);
        log.info("Booking updated: bookingId={}", booking.getId());

        String text = String.format("""
                        Your booking has been <b>successfully updated</b>.
                        
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
                        
                        The updated booking info
                        has been sent to your email address.
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

        sessionService.deleteAll(chatId);
        log.debug("Session cleared: chat id={}", chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());

        try {
            emailService.sendBookingNotification(booking, NotificationType.UPDATE);
        } catch (MessagingException exception) {
            log.error("CRITICAL: Failed to initiate email send for booking {}.", booking.getId(), exception);
        }
    }
}

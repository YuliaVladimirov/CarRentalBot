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
 * Callback handler responsible for confirming updates to an existing booking.
 * <p>Persists modified contact information, notifies the user, and clears session state.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmMyBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CONFIRM_MY_BOOKING";

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
     * Service for sending booking update notifications.
     */
    private final EmailService emailService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building the inline keyboard for post-update navigation.
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
     * Applies booking updates, notifies the user, and clears the session.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if booking id is missing from session
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

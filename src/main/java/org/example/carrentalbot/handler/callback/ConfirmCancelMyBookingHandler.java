package org.example.carrentalbot.handler.callback;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.EmailException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.NotificationType;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.service.EmailService;
import org.example.carrentalbot.service.ReminderService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Slf4j
@Component
public class ConfirmCancelMyBookingHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);
    public static final String KEY = "CONFIRM_CANCEL_MY_BOOKING";

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final ReminderService reminderService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public ConfirmCancelMyBookingHandler(BookingService bookingService,
                                         SessionService sessionService,
                                         EmailService emailService,
                                         ReminderService reminderService,
                                         TelegramClient telegramClient,
                                         KeyboardFactory keyboardFactory) {
        this.bookingService = bookingService;
        this.sessionService = sessionService;
        this.emailService = emailService;
        this.reminderService = reminderService;
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));

        Booking booking = bookingService.cancelBooking(bookingId);

        reminderService.cancelReminders(booking);

        String text = String.format("""
                    ✅ Booking successfully canceled.
                    Booking id: %s
                    
                    A confirmation of your booking cancellation
                    has been sent to your email address: <b>%s</b>
                    
                    You can start a new booking anytime from the main menu.
                    """, booking.getId(), booking.getEmail());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMainMenuKeyboard();

        sessionService.deleteAll(chatId);

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
            throw new EmailException("Failed to initiate email send.", exception);
        }
    }
}

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Slf4j
@Component
public class ConfirmBookingHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    private final SessionService sessionService;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final ReminderService reminderService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public ConfirmBookingHandler(SessionService sessionService,
                                 BookingService bookingService,
                                 EmailService emailService,
                                 ReminderService reminderService,
                                 TelegramClient telegramClient,
                                 KeyboardFactory keyboardFactory) {
        this.sessionService = sessionService;
        this.bookingService = bookingService;
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
        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));

        Integer totalDays = sessionService
                .getInteger(chatId, "totalDays")
                .orElseThrow(() -> new DataNotFoundException("Total days not found in session"));

        BigDecimal totalCost = sessionService
                .getBigDecimal(chatId, "totalCost")
                .orElseThrow(() -> new DataNotFoundException("Total cost not found in session"));

        String phone = sessionService
                .getString(chatId, "phone")
                .orElseThrow(() -> new DataNotFoundException("Phone not found in session"));

        String email = sessionService
                .getString(chatId, "email")
                .orElseThrow(() -> new DataNotFoundException("Email not found in session"));

        Booking booking = bookingService.createBooking(carId, callbackQuery.getFrom().getId(),
                startDate, endDate, totalDays, totalCost,
                phone, email);

        reminderService.createReminders(booking);

        String text = String.format("""
                Your booking has been <b>successfully confirmed</b>.
                Booking Id: <b>%s</b>
                
                The full booking info
                has been sent to your email address: <b>%s</b>
                
                Thank you for choosing our service! 🚗
                """, booking.getId(), booking.getEmail());

        sessionService.deleteAll(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());

        try {
            emailService.sendBookingNotification(booking, NotificationType.CONFIRMATION);
        } catch (MessagingException exception) {
            log.error("CRITICAL: Failed to initiate email send for booking {}.", booking.getId(), exception);
            throw new EmailException("Failed to initiate email send.", exception);
        }
    }
}
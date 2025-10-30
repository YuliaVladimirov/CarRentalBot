package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.*;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Component
public class ConfirmBookingHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final BookingService bookingService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public ConfirmBookingHandler(SessionService sessionService,
                                 NavigationService navigationService,
                                 BookingService bookingService,
                                 TelegramClient telegramClient,
                                 KeyboardFactory keyboardFactory) {
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.bookingService = bookingService;
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
                .orElseThrow(() -> new DataNotFoundException(chatId, "Car id not found"));

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException(chatId, "Start date not found in session"));

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException(chatId, "End date not found in session"));

        String phone = sessionService
                .getString(chatId, "phone")
                .orElseThrow(() -> new DataNotFoundException(chatId, "Phone not found in session"));

        String email = sessionService
                .getString(chatId, "email")
                .orElseThrow(() -> new DataNotFoundException(chatId, "Email not found in session"));

        BigDecimal totalCost = sessionService
                .getBigDecimal(chatId, "totalCost")
                .orElseThrow(() -> new DataNotFoundException(chatId, "Total cost not found in session"));

        Booking booking = bookingService.createBooking(chatId, carId, callbackQuery.getFrom().getId(),
                startDate, endDate, totalCost,
                phone, email);

        //create later email service and pass Booking booking to this service to send confirming email.

        String text = String.format("""
                Your booking has been <b>successfully confirmed</b>.
                Booking ID: <b>%s</b>
                
                The full booking info
                has been sent to your email address: <b>%s</b>
                
                Thank you for choosing our service! 🚗
                """, booking.getId(), booking.getEmail());

        sessionService.deleteAll(chatId);
        navigationService.push(chatId, KEY);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
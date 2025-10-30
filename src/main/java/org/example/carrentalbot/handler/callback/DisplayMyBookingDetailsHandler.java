package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Component
public class DisplayMyBookingDetailsHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);
    public static final String KEY = "MY_BOOKINGS_DETAILS";

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;


    public DisplayMyBookingDetailsHandler (BookingService bookingService,
                                           SessionService sessionService,
                                           NavigationService navigationService,
                                           TelegramClient telegramClient,
                                           KeyboardFactory keyboardFactory) {
        this.bookingService = bookingService;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
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

        UUID bookingId = updateBookingIdInSession(chatId, callbackQuery.getData());
        Booking booking = bookingService.getBookingById(chatId, bookingId);

        BigDecimal dailyRate = booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP);
        long totalDays = bookingService.calculateTotalDays(booking.getStartDate(), booking.getEndDate());

        String text = String.format("""
                        <b>Booking details:</b>
                        
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
                        """,
                booking.getId(),
                booking.getCar().getBrand(),
                booking.getCar().getModel(),
                booking.getCar().getCategory().getValue(),
                booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                totalDays,
                dailyRate,
                booking.getTotalCost(),
                booking.getPhone(),
                booking.getEmail(),
                booking.getStatus());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMyBookingDetailsKeyboard();

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private UUID updateBookingIdInSession(Long chatId, String callbackData) {
        UUID fromCallback = extractBookingIdFromCallback(chatId, callbackData);

        UUID fromSession = sessionService
                .getUUID(chatId, "bookingId")
                .orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException(chatId, "Booking id not found in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "bookingId", result);
        }

        return result;
    }

    private UUID extractBookingIdFromCallback(Long chatId, String callbackData) {

        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(idStr -> {
                    try {
                        return UUID.fromString(idStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException(chatId, "Invalid UUID format: " + idStr);
                    }
                })
                .orElse(null);
    }
}

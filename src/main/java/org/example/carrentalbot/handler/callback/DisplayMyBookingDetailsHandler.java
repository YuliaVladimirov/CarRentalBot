package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingServiceImpl;
import org.example.carrentalbot.session.SessionServiceImpl;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DisplayMyBookingDetailsHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);
    public static final String KEY = "MY_BOOKING_DETAILS";

    private final BookingServiceImpl bookingService;
    private final SessionServiceImpl sessionService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

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
        Booking booking = bookingService.getBookingById(bookingId);

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
                booking.getTotalDays(),
                booking.getCar().getDailyRate().setScale(0, RoundingMode.HALF_UP),
                booking.getTotalCost(),
                booking.getPhone(),
                booking.getEmail(),
                booking.getStatus());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMyBookingDetailsKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private UUID updateBookingIdInSession(Long chatId, String callbackData) {
        UUID fromCallback = extractBookingIdFromCallback(callbackData);

        UUID fromSession = sessionService
                .getUUID(chatId, "bookingId")
                .orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Booking id not found in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "bookingId", result);
        }

        return result;
    }

    private UUID extractBookingIdFromCallback(String callbackData) {

        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
                .map(idStr -> {
                    try {
                        return UUID.fromString(idStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException("Invalid UUID format: " + idStr);
                    }
                })
                .orElse(null);
    }
}

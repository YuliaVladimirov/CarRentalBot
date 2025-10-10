package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.service.*;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class ConfirmBookingHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_BOOKING";

    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final BookingService bookingService;
    private final TelegramClient telegramClient;

    public ConfirmBookingHandler(NavigationService navigationService,
                                 SessionService sessionService, BookingService bookingService,
                                 TelegramClient telegramClient) {
        this.navigationService = navigationService;
        this.sessionService = sessionService;
        this.bookingService = bookingService;

        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        UUID carId = sessionService.get(chatId, "carId", UUID.class).orElseThrow(() -> new DataNotFoundException(chatId, "Car id not found"));

        LocalDate startDate = sessionService.get(chatId, "startDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "Start date not found"));

        LocalDate endDate = sessionService.get(chatId, "endDate", LocalDate.class).orElseThrow(() -> new DataNotFoundException(chatId, "End date not found"));

        String phone = sessionService.get(chatId, "phone", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Phone not found"));

        String email = sessionService.get(chatId, "email", String.class).orElseThrow(() -> new DataNotFoundException(chatId, "Email not found"));

        BigDecimal totalCost = sessionService.get(chatId, "totalCost", BigDecimal.class).orElseThrow(() -> new DataNotFoundException(chatId, "Total cost not found"));

        Booking booking = bookingService.createBooking(chatId, carId, callbackQuery.getFrom().getId(),
                startDate, endDate, totalCost,
                phone, email);

        //create later email service and pass Booking booking to this service to send confirming email.

        String text = """
                Your booking has been <b>successfully confirmed</b>.

                An email with the full booking details has been sent to your email address:
                <b>user@example.com</b>

                Thank you for choosing our service! 🚗
                """;

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());
    }
}
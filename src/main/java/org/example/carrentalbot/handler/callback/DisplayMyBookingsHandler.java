package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

@Component
public class DisplayMyBookingsHandler implements CallbackHandler  {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    public static final String KEY = "MY_BOOKINGS";

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;


    public DisplayMyBookingsHandler(BookingService bookingService,
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
        sessionService.put(chatId, "flowContext", FlowContext.MY_BOOKINGS_FLOW);

        List<Booking> bookings = bookingService.getBookingsByCustomerTelegramId(callbackQuery.getFrom().getId());

        if (bookings.isEmpty()) {
            telegramClient.sendMessage(SendMessageDto.builder()
                    .chatId(chatId.toString())
                    .text("📭 You have no bookings at the moment.")
                    .parseMode("HTML")
                    .build());
            return;
        }

        for (Booking booking : bookings) {

            String text = String.format("""
                        <b>Booking: %s</b>

                        🚗  <b>%s %s  - %s</b>
                        📅  %s - %s
                        💰  €%s
                        %s
                        """,
                    booking.getId(),
                    booking.getCar().getBrand(),
                    booking.getCar().getModel(),
                    booking.getCar().getCategory().getValue(),
                    booking.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    booking.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    booking.getTotalCost(),
                    getStatusLabel(booking.getStatus()));

            InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMyBookingsKeyboard(booking.getId());

            telegramClient.sendMessage(SendMessageDto.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(replyMarkup)
                    .build());
        }

        navigationService.push(chatId, KEY);
    }

    public String getStatusLabel(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "🟢  Confirmed";
            case CANCELLED -> "🔴  Cancelled";
        };
    }
}

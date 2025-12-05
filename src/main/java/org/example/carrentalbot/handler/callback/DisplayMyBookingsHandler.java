package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayMyBookingsHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    public static final String KEY = "MY_BOOKINGS";

    private final BookingService bookingService;
    private final SessionService sessionService;
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
        log.info("Processing 'display my bookings' flow");

        sessionService.put(chatId, "flowContext", FlowContext.MY_BOOKINGS_FLOW);
        log.debug("Session updated: 'flowContext' set to {}", FlowContext.MY_BOOKINGS_FLOW);

        List<Booking> bookings = bookingService.getBookingsByCustomerTelegramId(callbackQuery.getFrom().getId());
        log.info("Fetched {} bookings for user: telegram user id={}", bookings.size(), callbackQuery.getFrom().getId());

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
    }

    public String getStatusLabel(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "🟢  Confirmed";
            case CANCELLED -> "🔴  Cancelled";
        };
    }
}

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

/**
 * Callback handler responsible for displaying user's bookings.
 * <p>Retrieves all bookings for the current user and presents them as interactive cards.
 * If no bookings exist, an empty-state message is shown.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayMyBookingsHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "MY_BOOKINGS";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler is globally accessible and can be triggered from any
     * conversational state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Service for retrieving user bookings.
     */
    private final BookingService bookingService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building booking management keyboards.
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
     * Loads and displays all bookings for the current user.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing user information
     */
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

    /**
     * Converts booking status into a display label.
     * @param status The status of the booking.
     * @return A formatted string representation of the status.
     */
    public String getStatusLabel(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "🟢  Confirmed";
            case CANCELLED -> "🔴  Cancelled";
        };
    }
}

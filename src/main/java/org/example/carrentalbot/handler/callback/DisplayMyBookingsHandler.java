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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service serves as the primary dashboard for user reservation management.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code DisplayMyBookingsHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Transitioning the conversational state to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Retrieving all historical and active bookings associated with a user's Telegram ID.</li>
 * <li>Handling "Empty State" scenarios where no bookings are found.</li>
 * <li>Iterating through retrieved bookings and presenting each as a detailed card with
 * context-specific management actions.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayMyBookingsHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code DisplayMyBookingsHandler} and properly route callbacks.
     */
    public static final String KEY = "MY_BOOKINGS";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Set to {@link EnumSet#allOf(Class)} to allow users to check their reservations from any
     * point in the application (e.g., via a persistent menu button).</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Service responsible for querying the persistence layer for booking records based on Telegram metadata.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for managing user-specific session data, specifically
     * for updating the current {@code flowContext} to ensure the bot
     * recognizes the user is now in management mode.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for each booking card,
     * embedding the specific {@code bookingId} for downstream actions.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * booking list or empty-state notifications.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@link #KEY}.
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * Returns the allowed contexts for this handler.
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Orchestrates the retrieval and display of the user's booking history.
     * <ol>
     * <li>Logs the initiation of the "My Bookings" view.</li>
     * <li><b>State Transition:</b> Switches the session context to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
     * <li>Queries {@link BookingService} using the user's unique Telegram ID.</li>
     * <li>If no bookings exist, sends a "📭 Empty" notification and terminates the flow.</li>
     * <li>Iterates through each {@link Booking}, formatting a rich-text card featuring:
     * <ul>
     * <li>Car brand, model, and category.</li>
     * <li>Formatted rental dates and total cost.</li>
     * <li>A color-coded status indicator via {@link #getStatusLabel}.</li>
     * </ul>
     * </li>
     * <li>Dispatches each card as a separate message with a specialized management keyboard.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query containing user metadata.
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
     * Translates a {@link BookingStatus} enum into a human-readable, emoji-enhanced label.
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

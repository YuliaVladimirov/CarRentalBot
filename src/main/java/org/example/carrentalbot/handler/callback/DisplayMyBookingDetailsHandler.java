package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service provides an exhaustive detailed view of a specific reservation.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code DisplayMyBookingDetailsHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Restricting accessibility to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Resolving the target {@code bookingId} from either the callback data or the existing session.</li>
 * <li>Synchronizing the session state to ensure downstream handlers have access to the active booking ID.</li>
 * <li>Fetching full relational data (Car, Category, Pricing) from the {@link BookingService}.</li>
 * <li>Presenting a comprehensive summary including daily rates, total costs, and contact details.</li>
 * <li>Displaying a specialized management keyboard for further interaction with the specific booking.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayMyBookingDetailsHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code DisplayMyBookingDetailsHandler} and properly route callbacks.
     */
    public static final String KEY = "MY_BOOKING_DETAILS";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#MY_BOOKINGS_FLOW} to ensure the user is
     * within the management context.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for fetching the complete {@link Booking} entity from the database.
     * <p>Provides access to car details, pricing, and the current status of the reservation.</p>
     */
    private final BookingService bookingService;

    /**
     * Service responsible for synchronizing the {@code bookingId} between the current request
     * and the persistent user session.
     * <p>Ensures that subsequent management actions (Edit/Cancel) know which specific
     * booking is being targeted.</p>
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the management keyboard, which includes
     * options for editing, cancelling, or returning to the main menu.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * detailed HTML summary to the user.
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
     * {@inheritDoc}
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Orchestrates the display of detailed booking information.
     * <ol>
     * <li>Extracts and persists the booking's {@link UUID} in the session via {@link #updateBookingIdInSession}.</li>
     * <li>Retrieves the {@link Booking} entity and its associated car specifications.</li>
     * <li>Formats a detailed HTML message including rental period, costs, and current status.</li>
     * <li>Sends the formatted summary with a management-specific inline keyboard.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The callback query potentially containing the {@code bookingId} in its data.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'display my booking details' flow");

        UUID bookingId = updateBookingIdInSession(chatId, callbackQuery.getData());

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

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

    /**
     * Synchronizes the selected booking ID between the incoming request and the persistent session.
     * <ol>
     * <li>Attempts to extract the {@link UUID} from the raw callback data.</li>
     * <li>Retrieves any previously stored booking ID from the {@link SessionService}.</li>
     * <li>Validates that at least one source provides a valid ID; otherwise, throws {@link DataNotFoundException}.</li>
     * <li>Updates the session with the active booking ID.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackData The raw data string from the Telegram callback.
     * @return The resolved {@link UUID} of the booking.
     * @throws DataNotFoundException if no ID is found in either the callback or the session.
     */
    private UUID updateBookingIdInSession(Long chatId, String callbackData) {
        UUID fromCallback = extractBookingIdFromCallback(callbackData);
        log.debug("Extracted from callback: booking id={}", fromCallback);

        UUID fromSession = sessionService
                .getUUID(chatId, "bookingId")
                .orElse(null);
        log.debug("Loaded from session: bookingId={}", fromSession);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Missing booking id in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "bookingId", result);
            log.debug("Session updated: 'bookingId' set to {}", result);
        } else {
            log.debug("Session unchanged: 'bookingId' remains {}", result);
        }

        return result;
    }

    /**
     * Parses the booking ID from the callback data string.
     * <p>Expected format: {@code KEY:UUID_STRING}.</p>
     * @param callbackData The raw callback string.
     * @return The parsed {@link UUID}, or {@code null} if the format is invalid or missing.
     * @throws InvalidDataException if the string contains a malformed UUID.
     */
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

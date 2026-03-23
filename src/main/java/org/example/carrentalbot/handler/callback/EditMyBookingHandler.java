package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service manages the entry point for modifying an existing reservation.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code EditMyBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Restricting execution to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Validating if an edit is permissible based on:</li>
 *  <ul>
 * <li>Status Check: Ensuring the booking has not already been canceled.</li>
 * <li>Temporal Check: Verifying the current date is at least one day prior to the rental start.</li>
 * </ul>
 * <li>Informing the user that date changes require a re-booking, while contact information remains editable.</li>
 * <li>Dispatches a context-aware HTML message and the appropriate markup</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditMyBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code EditMyBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "EDIT_MY_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#MY_BOOKINGS_FLOW} as editing is a
     * management-specific action.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for fetching the current state of the {@link Booking} from the database
     * to perform lifecycle validations.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for retrieving the {@code bookingId} previously synchronized
     * during the details display phase.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the editing keyboard or the keyboard routing back to the main menu
     * when validation fails.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver validation
     * warnings or the edit menu.
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
     * Orchestrates the validation and initiation of the editing flow.
     * <ol>
     * <li>Retrieves the active {@code bookingId} from the session.</li>
     * <li>Fetches the latest {@link Booking} data to ensure the UI reflects the current database state.</li>
     * <li><b>Validation Logic:</b>
     * <ul>
     * <li>If status is {@code CANCELLED}: Redirects to Main Menu with a warning.</li>
     * <li>If {@code today} is not before {@code startDate}: Denies editing due to the 24h policy.</li>
     * <li>Otherwise: Presents the edit menu for contact information updates.</li>
     * </ul>
     * </li>
     * <li>Dispatches a context-aware HTML message and the appropriate {@code replyMarkup}.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the {@code bookingId} is missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'edit my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        LocalDate today = LocalDate.now();

        String text;
        InlineKeyboardMarkupDto replyMarkup;

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            text = """
                    ⚠️ This booking has already been canceled
                    and cannot be edited.
                    
                    You can make a new booking from the main menu.
                    """;
            replyMarkup = keyboardFactory.buildToMainMenuKeyboard();
        } else if (!today.isBefore(booking.getStartDate())) {
            text = """
                    ⚠️ This booking can no longer be edited.
                    Changes can be made up to one day
                    before the rental start date.
                    
                    You can make a new booking from the main menu.
                    """;
            replyMarkup = keyboardFactory.buildToMainMenuKeyboard();
        } else {

            text = """
                    ⚠️ <i>To change your rental dates,</i>
                    <i>please cancel your current booking</i>
                    <i> and create a new one.</i>
                    
                    Update your contact info,
                    then press <b>Continue</b> when done.
                    """;

            replyMarkup = keyboardFactory.buildEditBookingKeyboard(ConfirmMyBookingHandler.KEY);
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

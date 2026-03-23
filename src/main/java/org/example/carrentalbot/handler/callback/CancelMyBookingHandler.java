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
 * <p>This service manages the intent to cancel an existing reservation from the
 * "My Bookings" management dashboard. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code CancelMyBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#MY_BOOKINGS_FLOW}.</li>
 * <li>Retrieving the active {@code bookingId} from the user's session.</li>
 * <li>Enforces business rules regarding cancellation eligibility (status and timing).</li>
 * <li>Prevents the cancellation of bookings that have already started or are already canceled.</li>
 * <li>Presents a confirmation prompt only if the booking is eligible for termination.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelMyBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code CancelMyBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CANCEL_MY_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#MY_BOOKINGS_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for fetching the current {@link Booking} state to validate against
     * cancellation business rules.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for retrieving the {@code bookingId} associated with the current interaction.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for building cancellation keyboard.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver validation
     * warnings or the confirmation prompt.
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
     * Orchestrates the validation logic for cancelling an existing booking.
     * <ol>
     * <li><b>State Retrieval:</b> Pulls the {@code bookingId} from the {@link SessionService}.</li>
     * <li><b>Eligibility Check:</b>
     * <ul>
     * <li>If {@code status} is already {@link BookingStatus#CANCELLED}: Informs the user the action is redundant.</li>
     * <li>If {@code today} is on or after {@code startDate}: Denies cancellation based on the 24-hour policy.</li>
     * <li>Otherwise: Requests a confirmation of the cancellation to proceed.</li>
     * </ul>
     * </li>
     * <li>Dispatches a context-specific message and the appropriate inline keyboard via {@link TelegramClient}.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the {@code bookingId} is missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'cancel my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        LocalDate today = LocalDate.now();

        String text;
        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            text = """
                    ⚠️ This booking has already been canceled.
                    
                    You can return to the main menu.
                    """;
        } else if (today.isEqual(booking.getStartDate()) || today.isAfter(booking.getStartDate())) {
            text = """
                    ⚠️ This booking can no longer be canceled.
                    
                    Cancellations are allowed
                    up to 1 day before the rental start date.
                    """;
        } else {
            text = "Are you sure you want to cancel this booking?";

            replyMarkup = keyboardFactory.buildCancelMyBookingKeyboard();
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build());
    }
}

package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
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
 * <p>This service performs the final verification of a vehicle's availability before
 * allowing a user to proceed with a booking. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code CheckCarAvailabilityHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Retrieving the targeted vehicle ID and the selected date range from the session.</li>
 * <li>Invoking the {@link BookingService} to check for overlapping reservations.</li>
 * <li>Handling the binary logic flow:
 * <ul>
 * <li><b>Available:</b> Encourages the user to proceed to the booking stage.</li>
 * <li><b>Unavailable:</b> Notifies the user and provides options to re-select dates or cars.</li>
 * </ul>
 * </li>
 * <li>Dispatching a status message with a context-aware keyboard (Success or Failure).</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckCarAvailabilityHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code CheckCarAvailabilityHandler} and properly route callbacks.
     */
    public static final String KEY = "CHECK_AVAILABILITY";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW} to ensure car availability check
     * only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service used to query existing bookings and determine scheduling conflicts.
     */
    private final BookingService bookingService;

    /**
     * Service responsible for managing user-specific session data, specifically
     * to access the stored {@code carId} and rental dates: {@code startDate} and {@code endDate}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for starting booking
     * or choosing other dates.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to deliver the availability status.
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
     * Executes the car availability check and updates the UI based on the result.
     * <ol>
     * <li>Retrieves {@code carId}, {@code startDate}, and {@code endDate} from the {@link SessionService}.</li>
     * <li>Calls {@link BookingService#isCarAvailable(UUID, LocalDate, LocalDate)} to perform the backend check.</li>
     * <li>Constructs an HTML response message based on the availability boolean.</li>
     * <li>Selects the appropriate reply markup from {@link KeyboardFactory}.</li>
     * <li>Sends a final status message to the user.</li>
     * </ol>
     * @param chatId The ID of the chat where the verification result should be sent.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if any of the three required session variables (ID, Start, End) are missing.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'check availability' flow");

        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));
        log.debug("Loaded from session: carId={}", carId);

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));
        log.debug("Loaded from session: startDate={}", startDate);

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));
        log.debug("Loaded from session: endDate={}", endDate);

        boolean available = bookingService.isCarAvailable(carId, startDate, endDate);
        log.info("Car availability checked: available={}", available);

        String carAvailable = """
                This car is <b>available</b> for your selected dates!
                
                You can proceed to booking.
                """;

        String carUnavailable = """
                Sorry, this car is <b>not available</b> for the selected dates.
                
                Please choose different dates or another car.
                """;

        String text = available ? carAvailable : carUnavailable;

        InlineKeyboardMarkupDto replyMarkup = available ? keyboardFactory.buildCarAvailableKeyboard() : keyboardFactory.buildCarUnavailableKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

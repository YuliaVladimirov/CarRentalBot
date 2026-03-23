package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service serves as the detailed view for a specific vehicle. It provides the
 * user with comprehensive information, including a visual representation and pricing.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code DisplayCarDetailsHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Updating and synchronizing the {@code carId} within the user's session.</li>
 * <li>Retrieving full vehicle specifications from the {@link CarService}.</li>
 * <li>Determining the appropriate call-to-action (Booking vs. Availability) based on the current browsing mode.</li>
 * <li>Dispatching a photo message with a detailed HTML caption and navigation markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayCarDetailsHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code DisplayCarDetailsHandler} and properly route callbacks.
     */
    public static final String KEY = "DISPLAY_CAR_DETAILS";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW} to ensure car details
     * are accessed only during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for retrieving detailed car information and imagery from the database.
     */
    private final CarService carService;

    /**
     * Service responsible for managing user-specific session data,
     * specifically the {@code carId} (in {@link UUID} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline contextual keyboard
     * that displays the next step options (Check Availability or Start Booking).
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to send photo-based messages with car details.
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
     * Processes the request to display detailed information for a specific car.
     * <ol>
     * <li>Logs the initiation of the detailed view flow.</li>
     * <li>Extracts and persists the vehicle's {@link UUID} in the session via {@link #updateCarIdInSession}.</li>
     * <li>Fetches the {@link Car} entity from the service.</li>
     * <li>Determines the next navigation step (Start Date selection or Start Booking) via {@link #getDataForKeyboard}.</li>
     * <li>Formats the vehicle's brand, model, description, and daily rate into an HTML caption.</li>
     * <li>Sends the car's image and information to the user using {@code sendPhoto}.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the car ID or browsing mode is missing.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'display car details' flow");

        UUID carId = updateCarIdInSession(chatId, callbackQuery.getData());

        Car car = carService.getCar(carId);
        log.info("Retrieved car: id={}", car.getId());

        Map.Entry<String, String> data = getDataForKeyboard(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarDetailsKeyboard(data.getKey(), data.getValue());

        String text = String.format("""
                🚘  <b>Car Details</b>:
                
                🏷️  Brand:  %s
                📌  Model:  %s
                📝  Description:  %s
                💰  Daily Rate:  €%s/day
                """, car.getBrand(), car.getModel(), car.getDescription(), car.getDailyRate().setScale(0, RoundingMode.HALF_UP));

        telegramClient.sendPhoto(SendPhotoDto.builder()
                .chatId(chatId.toString())
                .photo(car.getImageFileId())
                .caption(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    /**
     * Synchronizes the selected car's UUID between the incoming callback and the session.
     * <ol>
     * <li>Attempts to extract the {@link UUID} from the raw callback data.</li>
     * <li>Retrieves any previously stored car ID from the {@link SessionService}.</li>
     * <li>Validates that at least one source provides a valid ID; otherwise, throws {@link DataNotFoundException}.</li>
     * <li>Updates the session with the active car ID.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackData The raw data string from the Telegram callback query.
     * @return The active {@link UUID} for the vehicle.
     * @throws DataNotFoundException if no vehicle ID can be found in callback or session.
     */
    private UUID updateCarIdInSession(Long chatId, String callbackData) {
        UUID fromCallback = extractCarIdFromCallback(callbackData);
        log.debug("Extracted from callback: car id={}", fromCallback);

        UUID fromSession = sessionService
                .getUUID(chatId, "carId")
                .orElse(null);
        log.debug("Loaded from session: carId={}", fromSession);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Missing car id in callback or session");
        }

        UUID result = (fromCallback != null) ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carId", result);
            log.debug("Session updated: 'carId' set to {}", result);
        } else {
            log.debug("Session unchanged: 'carId' remains {}", result);
        }

        return result;
    }

    /**
     * Parses the callback data string to extract a {@link UUID}.
     * <p>Expected format: {@code KEY:UUID_STRING}.</p>
     * @param callbackData The raw callback string.
     * @return The parsed {@link UUID}, or {@code null} if parsing fails or data is missing.
     * @throws InvalidDataException if the ID string is malformed.
     */
    private UUID extractCarIdFromCallback(String callbackData) {

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

    /**
     * Logic-driven router that selects the next {@link CallbackHandler} key
     * based on the user's current {@link FlowContext}.
     * Retrieves the active {@link CarBrowsingMode} from the session.
     * @param chatId The ID of the chat.
     * @return A {@link Map.Entry} where the key is the next {@link CallbackHandler} KEY and the value is the button label.
     * @throws DataNotFoundException if the browsing mode is missing from the session.
     */
    private Map.Entry<String, String> getDataForKeyboard(Long chatId) {
        CarBrowsingMode carBrowsingMode = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElseThrow(() -> new DataNotFoundException("Car browsing mode not found in session"));
        log.debug("Loaded from session: carBrowsingMode={}", carBrowsingMode);

        return switch (carBrowsingMode) {
            case ALL_CARS -> Map.entry(AskForStartDateHandler.KEY, "🕒 Check Availability");
            case CARS_FOR_DATES -> Map.entry(StartBookingHandler.KEY, "📝 Start Booking");
        };
    }
}

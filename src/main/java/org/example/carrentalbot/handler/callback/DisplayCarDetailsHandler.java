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
 * Callback handler responsible for displaying detailed information about a selected car.
 * <p>Operates within the browsing flow and presents vehicle details along with
 * the next available user action.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayCarDetailsHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "DISPLAY_CAR_DETAILS";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the browsing flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service for retrieving car inventory data.
     */
    private final CarService carService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building contextual keyboards.
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
     * Displays detailed information about the selected car.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing car selection
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
     * Resolves and synchronizes the selected car ID with the user session.
     *
     * @return resolved car identifier
     * @throws DataNotFoundException if no car ID can be resolved from callback or session
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
     * Extracts a {@link UUID} from callback data.
     *
     * @param callbackData raw callback payload
     * @return parsed UUID or {@code null} if absent
     * @throws InvalidDataException if the value is invalid
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
     * Resolves the next action based on the current browsing mode.
     *
     * @param chatId chat identifier
     * @return key-label pair for the next action
     * @throws DataNotFoundException if browsing mode is missing
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

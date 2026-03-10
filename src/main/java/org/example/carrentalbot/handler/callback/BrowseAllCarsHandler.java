package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service is responsible for retrieving and displaying the complete list
 * of available vehicles within a specific category. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code BrowseAllCarsHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Updating and synchronizing the {@link CarBrowsingMode} within the user's session.</li>
 * <li>Retrieving filtered car data from the {@link CarService} based on the stored category.</li>
 * <li>Constructing the vehicle selection menu via {@link KeyboardFactory}.</li>
 * <li>Dispatching the vehicle-selection menu message with appropriate formatting and markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseAllCarsHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code BrowseAllCarsHandler} and properly route callbacks.
     */
    public static final String KEY = "BROWSE_ALL_CARS";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW}
     * to ensure the car search only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for querying vehicle inventory data from the database.
     */
    private final CarService carService;

    /**
     * Service responsible for managing user-specific session data, specifically the
     * chosen {@link CarCategory} and {@link CarBrowsingMode}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for selecting cars.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the car list in the selected category.
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
     * @return A set containing only {@link FlowContext#BROWSING_FLOW}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Processes the request to display all cars in the current category.
     * <ol>
     * <li>Logs the initiation of the "all cars" browsing flow.</li>
     * <li>Updates the user's session with the selected {@link CarBrowsingMode} via {@link #updateBrowsingModeInSession(Long, String)}.</li>
     * <li>Retrieves the previously selected {@link CarCategory} from the session, throwing {@link DataNotFoundException} if missing.</li>
     * <li>Fetches the list of {@link Car} entities matching the category from the {@link CarService}.</li>
     * <li>Invokes {@link KeyboardFactory} to build an inline keyboard populated with the retrieved car list.</li>
     * <li>Sends an HTML-formatted message to the user displaying the category name and vehicle options.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the required category is not found in the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'browse all cars' flow");

        updateBrowsingModeInSession(chatId, callbackQuery.getData());

        CarCategory carCategory = sessionService
                .getCarCategory(chatId, "carCategory")
                .orElseThrow(() -> new DataNotFoundException("Category not found in session"));
        log.debug("Loaded from session: carCategory={}", carCategory);

        List<Car> allCars = carService.getAllCarsByCategory(carCategory);
        log.info("Fetched {} cars for category '{}'", allCars.size(), carCategory);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarsKeyboard(allCars);

        String text = String.format("""
                <b>All cars in category '%s':</b>
                """, carCategory.getValue());

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    /**
     * Synchronizes the car browsing mode between the incoming callback and the existing session.
     * <ol>
     * <li>Extracts the {@link CarBrowsingMode} from the raw callback data.</li>
     * <li>Retrieves the currently stored mode from the {@link SessionService}.</li>
     * <li>Validates that a mode is present in either the callback or the session; otherwise, throws {@code DataNotFoundException}.</li>
     * <li>Persists the result to the session if a change is detected.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackData The raw callback data string.
     * @throws DataNotFoundException if no browsing mode is found in callback or session.
     */
    private void updateBrowsingModeInSession(Long chatId, String callbackData) {
        CarBrowsingMode fromCallback = extractBrowsingModeFromCallback(callbackData);
        log.debug("Extracted from callback: car browsing mode={}", fromCallback);

        CarBrowsingMode fromSession = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElse(null);
        log.debug("Loaded from session: carBrowsingMode={}", fromSession);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Missing car browsing mode in callback or session");
        }

        CarBrowsingMode result = fromCallback != null ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carBrowsingMode", result);
            log.debug("Session updated: 'carBrowsingMode' set to {}", result);
        } else {
            log.debug("Session unchanged: 'carBrowsingMode' remains {}", result);
        }
    }

    /**
     * Parses the callback data string to extract a {@link CarBrowsingMode}.
     * <p>Expected format: {@code KEY:CAR_BROWSING_MODE} (e.g., "BROWSE_ALL_CARS:ALL_CARS").</p>
     * @param callbackData The raw callback string.
     * @return The parsed {@link CarBrowsingMode}, or {@code null} if parsing fails or data is missing.
     * @throws InvalidDataException if the mode string does not match any known {@link CarBrowsingMode}.
     */
    private CarBrowsingMode extractBrowsingModeFromCallback(String callbackData) {
        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":")[1])
                .map(String::toUpperCase)
                .map(categoryStr -> {
                    try {
                        return CarBrowsingMode.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException("Invalid car browsing mode: " + categoryStr);
                    }
                })
                .orElse(null);
    }
}

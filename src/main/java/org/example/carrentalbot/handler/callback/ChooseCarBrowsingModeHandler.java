package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service acts as an intermediary step in the browsing flow, allowing users
 * to select how they wish to view cars within a chosen category. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ChooseCarBrowsingModeHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Extracting and persisting the selected {@link CarCategory} into the user's session.</li>
 * <li>Invokes {@link KeyboardFactory} to generate a keyboard based on the browsing mode ("All Cars" or "Cars For My Dates").</li>
 * <li>Sends the browsing mode selection message to the user via the Telegram API.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChooseCarBrowsingModeHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ChooseCarBrowsingModeHandler} and properly route callbacks.
     */
    public static final String KEY = "CHOOSE_CAR_BROWSING_MODE";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW}
     * to ensure browsing mode selection only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for managing user-specific session data, specifically the
     * currently selected {@link CarCategory}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for selecting the car browsing mode.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the car browsing mode selection menu.
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
     * Processes the request to select a car browsing mode.
     * <ol>
     * <li>Logs the browsing mode selection event.</li>
     * <li>Updates the session with the target category via {@link #updateCategoryInSession(Long, String)}.</li>
     * <li>Invokes {@link KeyboardFactory} to build the browsing mode keyboard.</li>
     * <li>Sends the "Choose browsing mode" message to the user via the Telegram API.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'choose browsing mode' flow");

        updateCategoryInSession(chatId, callbackQuery.getData());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarBrowsingModeKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Choose browsing mode:</b>")
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    /**
     * Synchronizes the car category between the incoming callback and the session.
     * <ol>
     * <li>Attempts to extract the {@link CarCategory} from the raw callback data.</li>
     * <li>Retrieves any previously stored category from the {@link SessionService}.</li>
     * <li>Validates that at least one source provides a valid category; otherwise, throws {@code DataNotFoundException}.</li>
     * <li>Updates the session with the active car category.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackData The raw data string from the Telegram callback query.
     * @throws DataNotFoundException if no category can be found in callback or session.
     */
    private void updateCategoryInSession(Long chatId, String callbackData) {
        CarCategory fromCallback = extractCategoryFromCallback(callbackData);
        log.debug("Extracted from callback: car category={}", fromCallback);

        CarCategory fromSession = sessionService
                .getCarCategory(chatId, "carCategory")
                .orElse(null);
        log.debug("Loaded from session: carCategory={}", fromSession);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Missing car category in callback or session");
        }

        CarCategory result = fromCallback != null ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carCategory", result);
            log.debug("Session updated: 'carCategory' set to {}", result);
        } else {
            log.debug("Session unchanged: 'carCategory' remains {}", result);
        }
    }

    /**
     * Parses the callback data string to extract a {@link CarCategory}.
     * <p>Expected format: {@code KEY:CAR_CATEGORY} (e.g., "CHOOSE_CAR_BROWSING_MODE:SUV").</p>
     * @param callbackData The raw callback string.
     * @return The parsed {@link CarCategory}, or {@code null} if parsing fails or data is missing.
     * @throws InvalidDataException if the category string does not match any known {@link CarCategory}.
     */
    private CarCategory extractCategoryFromCallback(String callbackData) {
        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":")[1])
                .map(String::toUpperCase)
                .map(categoryStr -> {
                    try {
                        return CarCategory.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        throw new InvalidDataException("Invalid category: " + categoryStr);
                    }
                })
                .orElse(null);
    }
}

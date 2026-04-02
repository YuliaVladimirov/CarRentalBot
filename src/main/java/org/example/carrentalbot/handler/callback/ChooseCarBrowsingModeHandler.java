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
 * Callback handler responsible for selecting the car browsing mode.
 * <p>Operates within the browsing flow and determines how cars should be presented
 * to the user (e.g., all cars or filtered by availability dates).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChooseCarBrowsingModeHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CHOOSE_CAR_BROWSING_MODE";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the browsing flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building car browsing mode keyboards.
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
     * Processes the car browsing mode selection.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing the selected browsing mode
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
     * Resolves and persists the selected car category into the user session.
     *
     * @throws DataNotFoundException if no category can be resolved from callback or session
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
     * Extracts a {@link CarCategory} from callback data.
     *
     * @param callbackData raw callback payload
     * @return parsed category or {@code null} if not present
     * @throws InvalidDataException if category is invalid
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

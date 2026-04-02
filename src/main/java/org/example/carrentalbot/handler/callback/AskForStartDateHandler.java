package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Callback handler responsible for initiating start date selection.
 *
 * <p>Operates within the browsing flow and presents an interactive calendar
 * for choosing the rental start date.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForStartDateHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "START_DATE";

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
     * Factory for building interactive inline calendar markup for selecting rental start date.
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
     * Displays a calendar for selecting the rental start date.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing browsing mode
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'start date calendar'");

        updateBrowsingModeInSession(chatId, callbackQuery.getData());

        YearMonth now = YearMonth.now();

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCalendar(
                now.getYear(), now.getMonthValue(), AskForEndDateHandler.KEY + ":");

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Pick your start date:")
                .replyMarkup(replyMarkup)
                .build());

    }

    /**
     * Resolves and synchronizes the car browsing mode with the user session.
     *
     * @throws DataNotFoundException if no browsing mode can be resolved from callback or session
     */
    private void updateBrowsingModeInSession(Long chatId, String callbackData) {
        CarBrowsingMode fromCallback = extractBrowsingModeFromCallback(callbackData);
        log.debug("Extracted from callback: carBrowsingMode={}", fromCallback);

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
     * Extracts a {@link CarBrowsingMode} from callback data.
     *
     * @param callbackData raw callback payload
     * @return parsed browsing mode or {@code null} if absent
     * @throws InvalidDataException if the mode is invalid
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

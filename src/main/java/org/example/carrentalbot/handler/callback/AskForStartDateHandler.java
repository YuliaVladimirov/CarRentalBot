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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service initiates the date selection sequence by presenting an interactive
 * calendar to the user. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code AskForStartDateHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Updating the user's session with the selected {@link CarBrowsingMode}.</li>
 * <li>Generating a dynamic calendar starting from the current month.</li>
 * <li>Configuring calendar buttons to route subsequent picks to the {@link AskForEndDateHandler}.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForStartDateHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code AskForStartDateHandler} and properly route callbacks.
     */
    public static final String KEY = "START_DATE";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW}
     * to ensure date selection only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for managing user-specific session data, specifically the
     * chosen {@link CarBrowsingMode}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the interactive inline calendar markup for selecting rental start date.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the calendar for rental date selection.
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
     * Processes the transition to the start-date selection step.
     * <ol>
     * <li>Logs the initiation of the start-date calendar flow.</li>
     * <li>Synchronizes the {@link CarBrowsingMode} from the callback into the user session.</li>
     * <li>Determines the current {@link YearMonth} to initialize the calendar view.</li>
     * <li>Invokes {@link KeyboardFactory} to build a calendar where each date button
     * is prefixed with the {@link AskForEndDateHandler#KEY}.</li>
     * <li>Sends the calendar interface to the user.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
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
     * Synchronizes the car browsing mode between the incoming callback and the existing session.
     * <ol>
     * <li>Extracts the {@link CarBrowsingMode} from the raw callback data.</li>
     * <li>Retrieves any existing mode from the {@link SessionService}.</li>
     * <li>Validates that a mode is present in either the callback or the session; otherwise, throws {@code DataNotFoundException}.</li>
     * <li>Persists the result to the session if a change is detected.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackData The raw callback data string.
     * @throws DataNotFoundException if no browsing mode is found in callback or session.
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
     * Parses the callback data string to extract a {@link CarBrowsingMode}.
     * <p>Expected format: {@code KEY:CAR_BROWSING_MODE} (e.g., "START_DATE:CARS_FOR_DATES").</p>
     * @param callbackData The raw callback string.
     * @return The parsed {@link CarBrowsingMode}, or {@code null} if parsing fails or data is missing.
     * @throws InvalidDataException if the mode string is malformed, unknown or does not match any known {@link CarBrowsingMode}.
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

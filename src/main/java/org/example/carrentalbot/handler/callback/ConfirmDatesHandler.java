package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CalendarAction;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service manages the second phase of the interactive calendar, focusing
 * on end-date selection and rental duration validation. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmDatesHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Handling calendar navigation for the end-date selection interface.</li>
 * <li>Retrieving and validating the {@code endDate} against the previously stored {@code startDate}.</li>
 * <li>Enforcing business rules regarding minimum (1 day) and maximum (60 days) rental periods.</li>
 * <li>Determining the next logical step based on the user's active {@link CarBrowsingMode}.</li>
 * <li>Dispatching confirmation summaries or error messages for invalid date ranges.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmDatesHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ConfirmDatesHandler} and properly route callbacks.
     */
    public static final String KEY = "CONFIRM_DATES";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW} to ensure date
     * confirmation only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for managing user-specific session data, specifically
     * to retrieve the {@code startDate} and persist the validated {@code endDate}
     * (both in {@link LocalDate} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for confirmation rental dates
     * or refreshing the calendar markup.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to send summaries of selected rental dates.
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
     * Processes interactions with the end-date calendar, including navigation
     * and final date validation.
     * <ol>
     * <li>Parses the callback data to extract the {@link CalendarAction}.</li>
     * <li>If the action is {@code PREV} or {@code NEXT}, the current month view
     * is updated via {@code editMessageReplyMarkup}.</li>
     * <li>If the action is {@code PICK}, the selected {@code endDate} is processed:</li>
     * <ul>
     * <li>The {@code startDate} is retrieved from the {@link SessionService}.</li>
     * <li>Validation checks are performed via {@link #validateEndDate} and {@link #validateDuration}.</li>
     * <li><b>Valid:</b> The {@code endDate} is saved, and a confirmation summary is displayed
     * with the next handler determined by {@link #getDataForKeyboard}.</li>
     * <li><b>Invalid:</b> An error message is sent outlining the specific constraints
     * (e.g., duration limit, past dates).</li>
     * </ul>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the {@code startDate} is missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm dates' flow");

        InlineKeyboardMarkupDto replyMarkup;
        Integer messageId = callbackQuery.getMessage().getMessageId();

        String data = callbackQuery.getData();
        String[] callbackParts = parseCallback(data);
        log.debug("Callback parsed: callback parts={}", (Object) callbackParts);

        CalendarAction action = extractCalendarAction(callbackParts);
        log.debug("Extracted calendar action: action={}", action);

        switch (action) {

            case IGNORE ->
                log.debug("Handling IGNORE action");

            case PREV, NEXT -> {
                log.debug("Handling month change: action={}, yearPart={}, monthPart={}",
                        action, callbackParts[2], callbackParts[3]);

                replyMarkup = handleMonthChange(callbackParts);

                telegramClient.sendEditMessageReplyMarkup(EditMessageReplyMarkupDto.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(replyMarkup)
                        .build());
            }

            case PICK -> {
                log.debug("Handling end date: action={}", action);

                LocalDate endDate = extractDate(callbackParts);
                log.debug("Extracted from callback: endDate={}", endDate);

                LocalDate startDate = sessionService
                        .getLocalDate(chatId, "startDate")
                        .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));
                log.debug("Loaded from session: startDate={}", startDate);

                String text;

                if (validateEndDate(startDate, endDate) && validateDuration(startDate, endDate)) {
                    log.debug("End date and duration are valid: start date={} , end date={}", startDate, endDate);

                    sessionService.put(chatId, "endDate", endDate);
                    log.debug("Session updated: 'endDate' set to {}", endDate);

                    String callbackKey = getDataForKeyboard(chatId);

                    text = String.format("""
                            Please confirm your dates:
                            
                            Start date: <b>%s</b>
                            End date: <b>%s</b>
                            """, startDate, endDate);

                    replyMarkup = keyboardFactory.buildConfirmDatesKeyboard(callbackKey);
                } else {
                    log.debug("End date or duration is invalid: start date={} , end date={}", startDate, endDate);

                    text = """
                            <b>Invalid end date or rental period:</b>
                            
                            ⚠️ <b>Make sure:</b>
                            • You cannot book for past days
                            • Start date must be before the end date.
                            • Minimum rental period is 1 day.
                            • Maximum rental period is 60 days.
                            
                            Please check your dates and re-enter:
                            """;

                    replyMarkup = keyboardFactory.buildInvalidDatesKeyboard();
                }

                telegramClient.sendMessage(SendMessageDto.builder()
                        .chatId(chatId.toString())
                        .text(text)
                        .parseMode("HTML")
                        .replyMarkup(replyMarkup)
                        .build());
            }
        }
    }

    /**
     * Splits the raw callback data into its constituent segments.
     * <p>The method expects a colon-delimited string (e.g., {@code "CONFIRM_DATES:PICK:2026-05-20"}).</p>
     * @param data The raw callback data string from the Telegram update.
     * @return A {@code String[]} containing the split parts of the callback.
     * @throws InvalidDataException if the data is null or empty.
     */
    private String[] parseCallback(String data) {
        return Optional.ofNullable(data)
                .orElseThrow(() -> new InvalidDataException("Callback data is null"))
                .trim()
                .split(":");
    }

    /**
     * Extracts and identifies the intended {@link CalendarAction} from the callback metadata.
     * <ol>
     * <li>Checks if the segmented data contains at least two parts.</li>
     * <li>Converts the second segment (index 1) to a {@link CalendarAction} enum.</li>
     * </ol>
     * @param callbackParts The segmented callback data.
     * @return The {@link CalendarAction} to be performed (PICK, NEXT, PREV, etc.).
     * @throws InvalidDataException if the action part is missing or does not match the enum.
     */
    private CalendarAction extractCalendarAction(String[] callbackParts) {
        if (callbackParts.length < 2) {
            throw new InvalidDataException("Missing calendar action in callback");
        }

        try {
            return CalendarAction.valueOf(callbackParts[1].toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidDataException("Invalid calendar action: " + callbackParts[1]);
        }
    }

    /**
     * Orchestrates the transition between calendar months by calculating the next
     * or previous month.
     * <ol>
     * <li>Extracts the current year and month from segments index 2 and 3.</li>
     * <li>Decrements the month for {@code PREV} actions, rolling back the year if necessary.</li>
     * <li>Increments the month for {@code NEXT} actions, rolling forward the year if necessary.</li>
     * <li>Requests a new calendar markup from the {@link KeyboardFactory} using the updated values.</li>
     * </ol>
     * @param callbackParts The segmented callback data containing the current view state.
     * @return A newly constructed {@link InlineKeyboardMarkupDto} for the adjacent month.
     * @throws InvalidDataException if the year or month segments are missing or malformed.
     */
    private InlineKeyboardMarkupDto handleMonthChange(String[] callbackParts) {
        if (callbackParts.length < 4) {
            throw new InvalidDataException("Missing year or month in callback");
        }

        int year = Integer.parseInt(callbackParts[2]);
        int month = Integer.parseInt(callbackParts[3]);

        if (callbackParts[1].equals("PREV")) {
            month--;
            if (month == 0) {
                month = 12;
                year--;
            }
        } else {
            month++;
            if (month == 13) {
                month = 1;
                year++;
            }
        }

        return keyboardFactory.buildCalendar(year, month, ConfirmDatesHandler.KEY + ":");
    }

    /**
     * Parses the date segment of the callback into a {@link LocalDate} object.
     * <p>Assumes the date is stored at index 2 of the callback segments in
     * standard ISO format (YYYY-MM-DD).</p>
     * @param callbackParts The segmented callback data.
     * @return The parsed {@link LocalDate}.
     * @throws InvalidDataException if the date segment is missing or has an invalid format.
     */
    private LocalDate extractDate(String[] callbackParts) {
        if (callbackParts.length < 3) {
            throw new InvalidDataException("Missing date in callback");
        }

        try {
            return LocalDate.parse(callbackParts[2]);
        } catch (DateTimeParseException exception) {
            throw new InvalidDataException("Invalid date format: " + callbackParts[2]);
        }
    }

    /**
     * Validates the chronological order of the selected dates.
     * <p>A date is valid if it is not in the past and not before the start date.</p>
     * @param startDate The stored start date of the rental.
     * @param endDate The newly selected end date of the rental.
     * @return {@code true} if the end date is today or later AND not before start date; {@code false} otherwise.
     */
    public boolean validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        return endDate.isAfter(startDate);
    }

    /**
     * Validates the duration of the rental period against business constraints.
     * <p>Current limits: Minimum of 0 days (same-day return) up to 60 days.</p>
     * @param startDate The stored start date.
     * @param endDate The selected end date.
     * @return {@code true} if the duration is between 0 and 60 days inclusive.
     */
    public boolean validateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);

        return days >= 1 && days <= 60;
    }

    /**
     * Logic-driven router that selects the next {@link CallbackHandler} key
     * based on the user's current {@link FlowContext}.
     * Retrieves the active {@link CarBrowsingMode} from the session.
     * @param chatId The ID of the chat.
     * @return The {@code KEY} associated with the next {@link CallbackHandler}.
     * @throws DataNotFoundException if the browsing mode is not present in the session.
     */
    private String getDataForKeyboard(Long chatId) {
        CarBrowsingMode carBrowsingMode = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElseThrow(() -> new DataNotFoundException("Car browsing mode not found in session"));
        log.debug("Loaded from session: carBrowsingMode={}", carBrowsingMode);

        return switch (carBrowsingMode) {
            case ALL_CARS -> CheckCarAvailabilityHandler.KEY;
            case CARS_FOR_DATES -> BrowseCarsForDatesHandler.KEY;
        };
    }
}

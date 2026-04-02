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
 * Callback handler responsible for processing chosen end date
 * and initiating confirmation of rental period.
 * <p>Operates within the browsing flow and handles calendar navigation,
 * date selection, and validation of rental duration.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmDatesHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CONFIRM_DATES";

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
     * Factory for building inline keyboard for confirmation rental dates
     * or refreshing the calendar markup.
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
     * Processes interactions with the end-date calendar, including navigation and final validation.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing calendar action and selected date
     * @throws DataNotFoundException if required session data (startDate) is missing
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
     * Splits callback data into structured parts.
     *
     * @param data raw callback payload
     * @return callback segments
     * @throws InvalidDataException if data is null
     */
    private String[] parseCallback(String data) {
        return Optional.ofNullable(data)
                .orElseThrow(() -> new InvalidDataException("Callback data is null"))
                .trim()
                .split(":");
    }

    /**
     * Extracts the requested calendar action from callback data.
     *
     * @param callbackParts parsed callback segments
     * @return resolved {@link CalendarAction}
     * @throws InvalidDataException if action is missing or invalid
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
     * Resolves target month based on navigation action and builds calendar markup.
     *
     * @param callbackParts parsed callback segments
     * @return updated calendar keyboard
     * @throws InvalidDataException if required date parts are missing
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
     * Extracts selected date from callback data.
     *
     * @param callbackParts parsed callback segments
     * @return parsed {@link LocalDate}
     * @throws InvalidDataException if date is missing or invalid
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
     * Validates that the selected end date is after the start date.
     *
     * @param startDate selected date
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        return endDate.isAfter(startDate);
    }

    /**
     * Validates that the rental duration is within allowed limits.
     *
     * @param startDate selected start date
     * @param endDate selected end date
     * @return {@code true} if both dates are present and the duration is between 1 and 60 days; {@code false} otherwise
     */
    public boolean validateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);

        return days >= 1 && days <= 60;
    }

    /**
     * Resolves the next handler key based on the user's current browsing mode.
     *
     * @param chatId chat identifier
     * @return next handler {@code KEY}
     * @throws DataNotFoundException if browsing mode is missing from session
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

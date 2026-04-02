package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.EditMessageReplyMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CalendarAction;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Callback handler responsible for processing the chosen start date
 * and initiating end date selection.
 * <p>Operates within the browsing flow and handles calendar navigation,
 * date selection, and transition to end date selection.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForEndDateHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "END_DATE";

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
     * Factory for building interactive inline calendar markup for selecting rental end date.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * Clock used for time-based validation to allow testability and consistent time access.
     */
    private final Clock clock;

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
     * Processes interactions with the start-date calendar, including navigation and start-date validation.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload containing calendar action and date
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'end date calendar'");

        String data = callbackQuery.getData();
        String[] callbackParts = parseCallback(data);
        log.debug("Callback parsed: callback parts={}", (Object) callbackParts);

        CalendarAction action = extractCalendarAction(callbackParts);
        log.debug("Extracted calendar action: action={}", action);

        Integer messageId = callbackQuery.getMessage().getMessageId();

        InlineKeyboardMarkupDto replyMarkup;

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
                log.debug("Handling start date: action={}", action);

                LocalDate startDate = extractDate(callbackParts);
                log.debug("Extracted from callback: start date={}", startDate);

                String text;

                if (validateStartDate(startDate)) {
                    log.debug("Start date is valid: start date={}", startDate);

                    sessionService.put(chatId, "startDate", startDate);
                    log.debug("Session updated: 'startDate' set to {}", startDate);

                    YearMonth yearMonth = YearMonth.from(startDate);

                    text = String.format("""
                            Start date selected: <b>%s</b>
                            Now pick the end date:
                            """, startDate);

                    replyMarkup = keyboardFactory.buildCalendar(yearMonth.getYear(), yearMonth.getMonthValue(), ConfirmDatesHandler.KEY + ":");

                } else {
                    log.debug("Start date is invalid: {}", startDate);

                    text = """
                            <b>Invalid start date:</b>
                            
                            ⚠️ <b>Make sure:</b>
                            • You cannot book for past days
                            • Start date must NOT be before the current date.
                            
                            Please check your date and re-enter:
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

        return keyboardFactory.buildCalendar(year, month, AskForEndDateHandler.KEY + ":");
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
     * Validates that the start date is not in the past.
     *
     * @param startDate selected date
     * @return {@code true} if valid; {@code false} otherwise
     */
    public boolean validateStartDate(LocalDate startDate) {
        if (startDate == null) return false;

        LocalDate today = LocalDate.now(clock);
        return !startDate.isBefore(today);
    }
}

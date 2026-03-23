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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service manages the interactive calendar state during the start-date selection
 * phase. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code AskForEndDateHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Enforcing access control by restricting execution to {@link FlowContext#BROWSING_FLOW}.</li>
 * <li>Handling calendar navigation (switching between months).</li>
 * <li>Processing the user's initial date selection ({@code PICK} action).</li>
 * <li>Validating that the selected start date is not in the past.</li>
 * <li>Persisting the valid start date to the {@link SessionService}.</li>
 * <li>Transitioning the user to the end-date selection by refreshing the calendar
 * with the {@link ConfirmDatesHandler} prefix.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForEndDateHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code AskForEndDateHandler} and properly route callbacks.
     */
    public static final String KEY = "END_DATE";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW}
     * to ensure date selection only occurs during the browsing lifecycle.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service responsible for managing user-specific session data, specifically the
     * validated {@code startDate} (in {@link LocalDate} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the interactive inline calendar markup
     * for selecting rental end date.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the calendar for rental date selection.
     */
    private final TelegramClient telegramClient;


    private final Clock clock = Clock.systemDefaultZone();

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
     * Processes interactions with the start-date calendar, including navigation
     * and date selection.
     * <ol>
     * <li>Parses the callback data to extract the specific {@link CalendarAction}.</li>
     * <li>If the action is {@code IGNORE}, the request is discarded.</li>
     * <li>If the action is {@code PREV} or {@code NEXT}, the current month view
     * is updated via {@code editMessageReplyMarkup}.</li>
     * <li>If the action is {@code PICK}, the selected date is validated:</li>
     * <ul>
     * <li><b>Valid:</b> The date is saved to the session, and a new calendar is
     * presented for the end-date selection.</li>
     * <li><b>Invalid:</b> An error message is sent along with a "Try Again"
     * keyboard option.</li>
     * </ul>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
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
     * Splits the raw callback data into its constituent parts for processing.
     * @param data The raw callback string from Telegram.
     * @return A string array containing the segments of the callback.
     * @throws InvalidDataException if the data is null.
     */
    private String[] parseCallback(String data) {
        return Optional.ofNullable(data)
                .orElseThrow(() -> new InvalidDataException("Callback data is null"))
                .trim()
                .split(":");
    }

    /**
     * Identifies the specific operation requested from the calendar (PICK, NEXT, PREV, IGNORE).
     * @param callbackParts The segmented callback data.
     * @return The corresponding {@link CalendarAction}.
     * @throws InvalidDataException if the action is missing or unrecognized.
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
     * Calculates the target year and month based on navigation input and
     * builds a new calendar markup.
     * @param callbackParts The segmented callback data containing current month/year.
     * @return A new {@link InlineKeyboardMarkupDto} representing the adjacent month.
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
     * Parses the date string from the callback data into a {@link LocalDate} object.
     * @param callbackParts The segmented callback data.
     * @return The selected {@link LocalDate}.
     * @throws InvalidDataException if the date segment is missing or malformed.
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
     * Validates that the selected start date is eligible for a new booking.
     * <p>The date is considered valid only if it is not earlier than the
     * current system date.</p>
     * @param startDate The date selected by the user.
     * @return {@code true} if the date is today or in the future; {@code false} otherwise.
     */
    public boolean validateStartDate(LocalDate startDate) {
        if (startDate == null) return false;

        LocalDate today = LocalDate.now(clock);
        return !startDate.isBefore(today);
    }
}

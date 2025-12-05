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

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmDatesHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_DATES";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final KeyboardFactory keyboardFactory;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

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

    private String[] parseCallback(String data) {
        return Optional.ofNullable(data)
                .orElseThrow(() -> new InvalidDataException("Callback data is null"))
                .trim()
                .split(":");
    }

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

    public boolean validateEndDate(LocalDate startDate, LocalDate endDate) {
        return !endDate.isBefore(LocalDate.now()) && !endDate.isBefore(startDate);
    }

    public boolean validateDuration(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);

        return days >= 0 && days <= 60;
    }

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

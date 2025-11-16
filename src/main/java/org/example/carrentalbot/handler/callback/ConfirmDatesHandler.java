package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CalendarAction;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

@Component
public class ConfirmDatesHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_DATES";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final KeyboardFactory keyboardFactory;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;

    public ConfirmDatesHandler(KeyboardFactory keyboardFactory,
                               SessionService sessionService,
                               TelegramClient telegramClient) {
        this.keyboardFactory = keyboardFactory;
        this.sessionService = sessionService;
        this.telegramClient = telegramClient;
    }

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

        InlineKeyboardMarkupDto replyMarkup;

        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        String[] callbackParts = data.split(":");
        CalendarAction action;

        try {
            action = CalendarAction.valueOf(callbackParts[1]);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid calendar action: " + callbackParts[1]);
        }

        switch (action) {

            case IGNORE -> { return; }

            case PREV, NEXT -> {
                replyMarkup = handleMonthChange(callbackParts);

                telegramClient.sendEditMessageReplyMarkup(EditMessageReplyMarkupDto.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(replyMarkup)
                        .build());
            }

            case PICK -> {
                LocalDate endDate = LocalDate.parse(callbackParts[2]);

                LocalDate startDate = sessionService
                        .getLocalDate(chatId, "startDate")
                        .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));

                String text;

                if (validateEndDate(startDate, endDate) && validateDuration(startDate, endDate)) {

                    sessionService.put(chatId, "endDate", endDate);

                    String callbackKey = getDataForKeyboard(chatId);

                    text = String.format("""
                            Please confirm your dates:
                            
                            Start date: <b>%s</b>
                            End date: <b>%s</b>
                            """, startDate, endDate);

                    replyMarkup = keyboardFactory.buildConfirmDatesKeyboard(callbackKey);
                } else {

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

    private InlineKeyboardMarkupDto handleMonthChange(String[] p) {

        int year = Integer.parseInt(p[2]);
        int month = Integer.parseInt(p[3]);

        if (p[1].equals("PREV")) {
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

        return switch (carBrowsingMode) {
            case ALL_CARS -> CheckCarAvailabilityHandler.KEY;
            case CARS_FOR_DATES -> BrowseCarsForDatesHandler.KEY;
        };
    }
}

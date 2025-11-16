package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Optional;

@Component
public class AskForStartDateHandler implements CallbackHandler {

    public static final String KEY = "START_DATE";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public AskForStartDateHandler(SessionService sessionService,
                                  KeyboardFactory keyboardFactory,
                                  TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
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

    private void updateBrowsingModeInSession(Long chatId, String callbackData) {
        CarBrowsingMode fromCallback = extractBrowsingModeFromCallback(callbackData);

        CarBrowsingMode fromSession = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElse(null);

        if (fromCallback == null && fromSession == null) {
            throw new DataNotFoundException("Car browsing mode not found in callback or session");
        }

        CarBrowsingMode result = fromCallback != null ? fromCallback : fromSession;

        if (!result.equals(fromSession)) {
            sessionService.put(chatId, "carBrowsingMode", result);
        }
    }


    private CarBrowsingMode extractBrowsingModeFromCallback(String callbackData) {
        return Optional.ofNullable(callbackData)
                .filter(data -> data.contains(":"))
                .map(data -> data.split(":", 2)[1])
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

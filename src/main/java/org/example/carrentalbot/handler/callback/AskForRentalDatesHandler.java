package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;

@Component
public class AskForRentalDatesHandler implements CallbackHandler {

    public static final String KEY = "ASK_FOR_RENTAL_DAYS";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final SessionService sessionService;
    private final TelegramClient telegramClient;

    public AskForRentalDatesHandler(SessionService sessionService,
                                    TelegramClient telegramClient) {
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

        updateBrowsingModeInSession(chatId, callbackQuery.getData());

        String text = """
                Please enter your rental period.
                
                Example: 05.10.2025 - 10.10.2025
                """;

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
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

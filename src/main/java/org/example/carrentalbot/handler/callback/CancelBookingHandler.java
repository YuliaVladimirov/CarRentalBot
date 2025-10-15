package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class CancelBookingHandler implements CallbackHandler {

    public static final String KEY = "CANCEL_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.EDIT_BOOKING_FLOW);

    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public CancelBookingHandler(SessionService sessionService, NavigationService navigationService,
                                TelegramClient telegramClient,
                                KeyboardFactory keyboardFactory) {
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
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

        FlowContext current = sessionService.get(chatId, "flowContext", FlowContext.class)
                .orElse(null);

        if (current == FlowContext.BOOKING_FLOW) {
            sessionService.put(chatId, "flowContext", FlowContext.EDIT_BOOKING_FLOW);
        }

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCancelBookingKeyboard();

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Are you sure you want to cancel this booking?")
                .replyMarkup(replyMarkup)
                .build());
    }
}

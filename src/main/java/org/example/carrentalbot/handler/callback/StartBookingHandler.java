package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class StartBookingHandler implements CallbackHandler {

    public static final String KEY = "START_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public StartBookingHandler(SessionService sessionService,
                               KeyboardFactory keyboardFactory,
                               TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getKey() { return KEY; }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        sessionService.put(chatId, "flowContext", FlowContext.BOOKING_FLOW);

        String text = """
                <b>Starting a New Booking</b>

                Please provide contact information step by step.
                You can cancel anytime before confirming your booking.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildStartBookingKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

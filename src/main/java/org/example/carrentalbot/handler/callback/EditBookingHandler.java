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
public class EditBookingHandler implements CallbackHandler {

    public static final String KEY = "EDIT_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW);

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;
    private final NavigationService navigationService;
    private final SessionService sessionService;

    public EditBookingHandler(
            TelegramClient telegramClient,
            KeyboardFactory keyboardFactory,
            NavigationService navigationService, SessionService sessionService
    ) {
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
        this.navigationService = navigationService;
        this.sessionService = sessionService;
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

        sessionService.put(chatId, "flowContext", FlowContext.EDIT_BOOKING_FLOW);

        String text = """
                <b>Edit your booking details:</b>

                ⚠️
                <i>To change rental dates,</i>
                <i>please cancel this booking</i>
                <i>and start a new one.</i>
                
                To edit your contact details
                please choose an option below:
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildEditBookingKeyboard();

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

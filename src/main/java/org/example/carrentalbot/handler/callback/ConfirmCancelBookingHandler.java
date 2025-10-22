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
public class ConfirmCancelBookingHandler implements CallbackHandler {

    public static final String KEY = "CONFIRM_CANCEL_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.EDIT_BOOKING_FLOW);

    private final TelegramClient telegramClient;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;

    public ConfirmCancelBookingHandler(TelegramClient telegramClient,
                                       SessionService sessionService,
                                       NavigationService navigationService,
                                       KeyboardFactory keyboardFactory) {
        this.telegramClient = telegramClient;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
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

        String text = """
                <b>Your booking has been cancelled.</b>

                All entered data was discarded.
                You can start a new booking anytime from the main menu.
                """;

        sessionService.clear(chatId);
        navigationService.clear(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

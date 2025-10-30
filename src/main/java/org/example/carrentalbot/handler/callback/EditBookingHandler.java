package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class EditBookingHandler implements CallbackHandler {

    public static final String KEY = "EDIT_BOOKING";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW, FlowContext.MY_BOOKINGS_FLOW);

    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public EditBookingHandler(SessionService sessionService,
                              NavigationService navigationService,
                              KeyboardFactory keyboardFactory,
                              TelegramClient telegramClient
    ) {
        this.sessionService = sessionService;
        this.navigationService = navigationService;
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
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException(chatId, String.format("Flow context not found in session for chat: %s", chatId)));

        if (flowContext == FlowContext.BOOKING_FLOW) {
            sessionService.put(chatId, "flowContext", FlowContext.EDIT_BOOKING_FLOW);
        }

        String text = """
                ⚠️ <i>To change rental dates,</i>
                <i>please create a new booking.</i>
                
                <b>Edit Contact Info:</b>
                
                Update your phone or email below,
                then press <b>Continue</b> when done.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildEditBookingKeyboard(DisplayBookingDetailsHandler.KEY);

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

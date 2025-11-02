package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.handler.callback.*;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class ConfirmPhoneHandler implements TextHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?\\d[\\d\\s]{7,14}\\d");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public ConfirmPhoneHandler(SessionService sessionService,
                               KeyboardFactory keyboardFactory,
                               TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public boolean canHandle(String text) {
        return text != null && PHONE_PATTERN.matcher(text.trim()).matches();
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        String phone = extractPhoneFromMessageText(message.getText());
        sessionService.put(chatId, "phone", phone);

        String text = String.format("""
                Confirm your phone:
                <b>%s</b>
                
                Press <b>OK</b> to continue
                or enter a new number.
                """, phone);

        String callbackKey = getDataForKeyboard(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildOkKeyboard(callbackKey);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private String extractPhoneFromMessageText(String text) {

        return Optional.ofNullable(text)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(t -> PHONE_PATTERN.matcher(t).matches())
                .orElse(null);
    }

    private String getDataForKeyboard(Long chatId) {
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session."));

        return switch (flowContext) {
            case BOOKING_FLOW -> AskForEmailHandler.KEY;
            case MY_BOOKINGS_FLOW -> EditMyBookingHandler.KEY;
            case EDIT_BOOKING_FLOW -> EditBookingHandler.KEY;
            default -> throw new InvalidStateException("Unexpected flow context for current handler: " + flowContext);
        };
    }
}

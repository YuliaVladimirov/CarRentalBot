package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidFlowContextException;
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

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);
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

        FlowContext flowContext = sessionService.get(chatId, "flowContext", FlowContext.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Flow context not found."));

        if (flowContext != FlowContext.BOOKING_FLOW
                && flowContext != FlowContext.EDIT_BOOKING_FLOW) {
            throw new InvalidFlowContextException(chatId,
                    "⚠️ This action is not allowed in current state. Please start from the main menu.");
        }

        String phone = retrievePhone(chatId, message.getText());

        String text = String.format("""
                You entered:
                Phone number: <b>%s</b>

                Please confirm or enter again.
                """, phone);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildConfirmPhoneKeyboard(flowContext);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private String retrievePhone(Long chatId, String text) {

        String phoneFromMessageText = extractPhoneFromMessageText(text);

        String phoneFromSession = sessionService.get(chatId, "phone", String.class).orElse(null);

        if (phoneFromMessageText == null && phoneFromSession == null) {
            throw new DataNotFoundException(chatId, "❌ Phone number not found in message or session");
        }

        String result = phoneFromMessageText != null ? phoneFromMessageText : phoneFromSession;

        if (!result.equals(phoneFromSession)) {
            sessionService.put(chatId, "phone", result);
        }
        return result;
    }

    private String extractPhoneFromMessageText(String text) {

        return Optional.ofNullable(text)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(t -> PHONE_PATTERN.matcher(t).matches())
                .orElse(null);
    }
}

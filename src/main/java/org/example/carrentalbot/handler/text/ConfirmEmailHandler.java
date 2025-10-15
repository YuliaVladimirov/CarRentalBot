package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class ConfirmEmailHandler implements TextHandler  {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public ConfirmEmailHandler(SessionService sessionService,
                               KeyboardFactory keyboardFactory,
                               TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }


    @Override
    public boolean canHandle(String text) {
        return text != null && EMAIL_PATTERN.matcher(text.trim()).matches();
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        String email = retrieveEmail(chatId, message.getText());

        String text = String.format("""
                You entered:
                Email: <b>%s</b>

                Please confirm or enter again.
                """, email);

        FlowContext flowContext = sessionService.get(chatId, "flowContext", FlowContext.class).orElseThrow(() -> new DataNotFoundException(chatId, "Data not found"));

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildConfirmEmailKeyboard(flowContext);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private String retrieveEmail(Long chatId, String text) {

        String emailFromMessageText = ExtractEmailFromMessageText(text);

        String emailFromSession = sessionService.get(chatId, "email", String.class).orElse(null);

        if (emailFromMessageText == null && emailFromSession == null) {
            throw new DataNotFoundException(chatId, "❌ Email not found in message or session");
        }

        String result = emailFromMessageText != null ? emailFromMessageText : emailFromSession;

        if (!result.equals(emailFromSession)) {
            sessionService.put(chatId, "email", result);
        }
        return result;
    }


    private String ExtractEmailFromMessageText(String text) {
        return Optional.ofNullable(text)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .filter(t -> EMAIL_PATTERN.matcher(t).matches())
                .orElse(null);
    }
}

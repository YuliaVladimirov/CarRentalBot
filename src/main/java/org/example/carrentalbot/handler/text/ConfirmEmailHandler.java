package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.handler.callback.DisplayBookingDetailsHandler;
import org.example.carrentalbot.handler.callback.EditBookingHandler;
import org.example.carrentalbot.handler.callback.EditMyBookingHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmEmailHandler implements TextHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    @Override
    public boolean canHandle(String text) {
        return text != null && EMAIL_PATTERN.matcher(text.trim()).matches();
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, String email) {
        log.info("Processing 'confirm email' flow");

        sessionService.put(chatId, "email", email);
        log.debug("Session updated: 'email' set to {}", email);

        String text = String.format("""
                Confirm your email:
                <b>%s</b>

                Press <b>OK</b> to continue
                or enter a new email.
                """, email);

        String callbackKey = getDataForKeyboard(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildOkKeyboard(callbackKey);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private String getDataForKeyboard(Long chatId) {
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session."));
        log.debug("Loaded from session: flowContext={}", flowContext);

        return switch (flowContext) {
            case BOOKING_FLOW -> DisplayBookingDetailsHandler.KEY;
            case EDIT_BOOKING_FLOW -> EditBookingHandler.KEY;
            case MY_BOOKINGS_FLOW -> EditMyBookingHandler.KEY;
            default -> throw new InvalidStateException("Unexpected flow context for current handler: " + flowContext);
        };
    }
}

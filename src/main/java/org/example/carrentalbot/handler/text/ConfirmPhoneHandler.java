package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.handler.callback.*;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * Concrete implementation of the {@link TextHandler} interface.
 * <p>Handles user phone number input: validates, stores it in session,
 * and requests confirmation before continuing the flow.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPhoneHandler implements TextHandler {

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler is globally accessible and can be triggered from any
     * conversational state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Regex pattern used to validate phone numbers.
     * <p>Supports international and local formats with optional '+' and spaces.</p>
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?\\d[\\d\\s]{7,14}\\d");

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building navigation keyboards.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(String text) {
        return text != null && PHONE_PATTERN.matcher(text.trim()).matches();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Processes and confirms the phone number.
     * <p>Stores the number in session and asks the user to confirm before continuing.</p>
     *
     * @param chatId chat identifier
     * @param phone validated phone number
     */
    @Override
    public void handle(Long chatId, String phone) {
        log.info("Processing 'confirm phone' flow");

        sessionService.put(chatId, "phone", phone);
        log.debug("Session updated: 'phone' set to {}", phone);

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

    /**
     * Determines next step based on current flow context.
     *
     * @param chatId chat identifier
     * @return callback key for next handler
     */
    private String getDataForKeyboard(Long chatId) {
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session."));
        log.debug("Loaded from session: flowContext={}", flowContext);

        return switch (flowContext) {
            case BOOKING_FLOW -> AskForEmailHandler.KEY;
            case MY_BOOKINGS_FLOW -> EditMyBookingHandler.KEY;
            case EDIT_BOOKING_FLOW -> EditBookingHandler.KEY;
            default -> throw new InvalidStateException("Unexpected flow context for current handler: " + flowContext);
        };
    }
}

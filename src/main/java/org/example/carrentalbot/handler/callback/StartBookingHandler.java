package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Callback handler responsible for starting the booking flow.
 *
 * <p>Operates within the browsing flow and transitions the user into the booking
 * process, providing initial instructions and the next interaction step.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "START_BOOKING";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the browsing flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building the inline keyboard for the initial booking step.
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
    public String getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Transitions the user into the booking flow and sends initial instructions.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'start booking' flow");

        sessionService.put(chatId, "flowContext", FlowContext.BOOKING_FLOW);
        log.debug("Session updated: 'flowContext' set to {}", FlowContext.BOOKING_FLOW);

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

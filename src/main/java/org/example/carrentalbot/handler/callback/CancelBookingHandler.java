package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Callback handler responsible for initiating booking cancellation.
 * <p>Acts as a confirmation step to prevent accidental cancellation during
 * booking or editing flows.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CANCEL_BOOKING";

    /**
     * Allowed flow contexts for this handler.
     * Handler is available during booking and booking-editing flows.
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Factory for building cancellation confirmation keyboards.
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
     * Displays a cancellation confirmation prompt to the user.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'cancel booking' flow");

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCancelBookingKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Are you sure you want to cancel this booking?")
                .replyMarkup(replyMarkup)
                .build());
    }
}

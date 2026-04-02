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
 * Callback handler responsible for finalizing booking cancellation.
 * <p>Clears all session data and returns the user to the main menu.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmCancelBookingHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CONFIRM_CANCEL_BOOKING";

    /**
     * Allowed flow contexts for this handler.
     * Handler is available during booking and booking-editing flows.
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

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
     * Clears the current booking session and confirms cancellation to the user.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm cancel booking' flow");

        String text = """
                <b>Your booking has been cancelled.</b>

                All entered data was discarded.
                You can start a new booking anytime from the main menu.
                """;

        sessionService.deleteAll(chatId);
        log.debug("Session cleared: chat id={}", chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

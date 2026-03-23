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
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service acts as the "Catch-All" or "Dead Letter" handler for the
 * callback routing system. It is responsible for:
 * <ul>
 * <li>Providing a unique internal key {@code __FALLBACK__} for unmapped requests.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Gracefully handling scenarios where a callback key {@code "__FALLBACK__"} is recognized
 * preventing the bot from becoming unresponsive.</li>
 * <li>Guiding the user back to the primary navigation points (Main Menu or Help).</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackCallbackHandler implements CallbackHandler {

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure that a safety
     * response can be delivered regardless of the user's current session state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory responsible for constructing the inline contextual keyboard
     * for main menu navigation.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to deliver the fallback message.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The string {@code "__FALLBACK__"}.
     */
    @Override
    public String getKey() {
        return "__FALLBACK__";
    }

    /**
     * {@inheritDoc}
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Processes an unrecognized or unsupported callback request.
     * <ol>
     * <li>Logs the fallback event for administrative monitoring.</li>
     * <li>Constructs a user-friendly HTML error message.</li>
     * <li>Provides actionable instructions for the user to reset their state
     * using navigation keyboard, or commands {@code /main} or {@code /help}.</li>
     * <li>Delivers the response message via the Telegram API.</li>
     * </ol>
     * @param chatId The ID of the chat where the error occurred.
     * @param callbackQuery The incoming callback query DTO that could not be routed.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'callback fallback'");

        String text = """
                ⚠️ Sorry, this button is not supported.
                
                Please return to the main menu (or type /main).
                For other available options type /help.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

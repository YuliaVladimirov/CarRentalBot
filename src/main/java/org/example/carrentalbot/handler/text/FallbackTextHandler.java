package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link TextHandler} interface.
 * <p>This service acts as the "Catch-All" or "Dead Letter" handler for the
 * text processing system. It is responsible for:
 * <ul>
 * <li>Ensuring a fail-safe response when no specialized {@link TextHandler} matches user input.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Providing a graceful rejection of unrecognized text, preventing the bot from becoming unresponsive.</li>
 * <li>Guiding the user back to valid conversational entry points (Main Menu or Help).</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackTextHandler implements TextHandler {

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
     * specifically for delivering the fallback message.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * <p>This implementation always returns {@code false} as it is intended to be
     * invoked explicitly by the dispatcher when all other matching attempts fail.</p>
     * @return Always {@code false}.
     */
    @Override
    public boolean canHandle(String text) {
        return false;
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
     * Processes an unrecognized or unsupported text message.
     * <ol>
     * <li>Logs the text fallback event for administrative monitoring.</li>
     * <li>Constructs a user-friendly HTML error message explaining the lack of comprehension.</li>
     * <li>Provides actionable instructions for the user to reset their state
     * using navigation keyboard, or commands {@code /main} or {@code /help}.</li>
     * <li>Delivers the response message via the Telegram API.</li>
     * </ol>
     * @param chatId The ID of the chat where the unhandled text originated.
     * @param textInput The raw text input that could not be processed.
     */
    @Override
    public void handle(Long chatId, String textInput) {
        log.info("Processing 'text fallback'");

        String text = """
                ⚠️ Sorry, I did not understand that text.
                
                Please reenter or return to the main menu (or type /main)
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

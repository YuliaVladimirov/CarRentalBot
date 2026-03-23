package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>Acts as the "Catch-All" or "Dead Letter" handler for the command routing system.
 * It is responsible for:
 * <ul>
 * <li>Ensuring a fail-safe response when no specialized {@link CommandHandler} matches user input.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Providing a graceful rejection of unrecognized command, preventing the bot from becoming unresponsive.</li>
 * <li>Guiding the user back to valid conversational entry points (Main Menu or Help).</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackCommandHandler implements CommandHandler {

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
    public String getCommand() {
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
     * Processes unrecognized commands by sending a help-oriented response.
     * <ol>
     * <li>Logs the text fallback event for administrative monitoring.</li>
     * <li>Constructs a user-friendly HTML error message explaining the lack of comprehension.</li>
     * <li>Provides actionable instructions for the user to reset their state
     * using navigation keyboard, or commands {@code /main} or {@code /help}.</li>
     * <li>Delivers the response message via the Telegram API.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param from User metadata (unused in fallback).
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing 'command fallback'");

        String text = """
                ⚠️ Sorry, I did not understand that command.
                
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

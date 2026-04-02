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

import static org.example.carrentalbot.util.HandlerRegistry.FALLBACK_KEY;

/**
 * Fallback command handler used when no command matches user input.
 * <p>Provides a safe default response and guides the user back to the main menu. This handler is available globally.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackCommandHandler implements CommandHandler {

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler is globally accessible and can be triggered from any
     * conversational state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

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
    public String getCommand() {
        return FALLBACK_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Handles unsupported commands by notifying the user and offering navigation options.
     *
     * @param chatId chat identifier
     * @param from user metadata
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

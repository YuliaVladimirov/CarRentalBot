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
 * Fallback text handler used when no other handler can process the input.
 * <p>Provides a safe default response and guides the user back to valid navigation options.
 * This handler is available globally.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackTextHandler implements TextHandler {

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
    public boolean canHandle(String text) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Handles unsupported text input by notifying the user and offering navigation options.
     *
     * @param chatId chat identifier
     * @param textInput user input text
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

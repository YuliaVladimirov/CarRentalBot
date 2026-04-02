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

import static org.example.carrentalbot.util.HandlerRegistry.FALLBACK_KEY;

/**
 * Fallback implementation of {@link CallbackHandler} used when no other handler
 * matches the incoming callback data.
 * <p>This handler ensures the system remains responsive for unrecognized or
 * unsupported callback interactions by providing a safe navigation path back
 * to the main menu.</p>
 * <p>It is registered using the global fallback key defined in the handler
 * registry and acts as a safety net for the callback routing system.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FallbackCallbackHandler implements CallbackHandler {

    /**
     * Allowed execution contexts for the fallback handler.
     * <p>This handler is available in all {@link FlowContext} states to ensure
     * the system can always recover from invalid or unknown callback interactions.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory for building navigation keyboards, including return-to-main-menu actions.
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
     * Handles unrecognized or unsupported callback interactions.
     * <p>Logs the event and responds with a user-friendly message guiding the user
     * back to the main menu or help section.</p>
     *
     * @param chatId chat identifier where the callback originated
     * @param callbackQuery unrecognized callback payload
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

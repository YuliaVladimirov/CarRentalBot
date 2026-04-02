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
 * Callback handler responsible for navigating the user to the main menu.
 *
 * <p>This handler acts as a global navigation entry point, allowing users to
 * reset or exit any workflow and return to the primary bot interface.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainMenuHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "GO_TO_MAIN_MENU";

    /**
     * Allowed flow contexts for this handler.
     * <p>The main menu is globally accessible and can be triggered from any
     * conversational state as a universal navigation reset point.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory for constructing main menu keyboards.
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
     * Displays the main navigation menu to the user.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload triggering navigation
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'main menu'");

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Main Menu:</b>")
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

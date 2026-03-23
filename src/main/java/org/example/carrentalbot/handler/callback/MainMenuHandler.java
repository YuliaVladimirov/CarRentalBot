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
 * <p>This service acts as the primary navigation hub, allowing users to return to
 * the main interface of the bot. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code MainMenuHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Constructing the primary navigation menu via {@link KeyboardFactory}.</li>
 * <li>Dispatching the main menu message with appropriate formatting and markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainMenuHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code MainMenuHandler} and properly route callbacks.
     */
    public static final String KEY = "GO_TO_MAIN_MENU";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Uses {@link EnumSet#allOf(Class)} to ensure the main menu is accessible
     * as a global escape or reset point from any conversational flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory responsible for constructing the main menu inline keyboard.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages, specifically to display the main menu.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@link #KEY}.
     */
    @Override
    public String getKey() {
        return KEY;
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
     * Processes the request to display the main navigation menu.
     * <ol>
     * <li>Logs the execution of the main menu navigation.</li>
     * <li>Invokes the {@link KeyboardFactory} to build the standard main menu keyboard.</li>
     * <li>Sends a new message to the user with HTML formatting and the navigation markup.</li>
     * </ol>
     * @param chatId The ID of the chat where the main menu should be displayed.
     * @param callbackQuery The incoming callback query DTO.
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

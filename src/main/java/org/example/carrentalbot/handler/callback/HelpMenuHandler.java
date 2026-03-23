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
 * <p>This service provides users with instructional content and a summary of available
 * bot commands. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code HelpMenuHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Generating a formatted HTML help guide including command descriptions.</li>
 * <li>Dispatching the help interface with an associated navigational keyboard.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelpMenuHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code HelpMenuHandler} and properly route callbacks.
     */
    public static final String KEY = "HELP";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Uses {@link EnumSet#allOf(Class)} to ensure assistance is available
     * regardless of the user's current transactional state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory responsible for constructing the help menu inline keyboard.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to display the help menu.
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
     * Processes the request to display the help and commands menu.
     * <ol>
     * <li>Logs the activation of the help menu.</li>
     * <li>Defines the structured help text using HTML formatting for commands and contact info.</li>
     * <li>Invokes the {@link KeyboardFactory} to build the help-specific keyboard markup.</li>
     * <li>Sends the help guide to the user, specifying HTML parse mode for proper rendering.</li>
     * </ol>
     * @param chatId The ID of the chat where the help information should be sent.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'help menu'");

        String helpText = """
                <b>Help & Commands</b>

                This bot helps you manage
                your car rentals — view, edit,
                or cancel bookings easily.

                <b>/start</b> — Begin or restart the conversation with the bot
                <b>/main</b> — Return to the main menu
                <b>/help</b> — Show this help message
                <b>/browse</b> — Browse available cars for rental
                <b>/bookings</b> - See all your bookings

                Use the on-screen buttons to navigate — no need to type commands manually.
                Changes can be made up to one day before your rental begins.

                For further assistance, contact us at <i>support@example.com</i>.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildHelpMenuKeyboard();

        telegramClient.sendMessage(
                SendMessageDto.builder()
                        .chatId(chatId.toString())
                        .text(helpText)
                        .parseMode("HTML")
                        .replyMarkup(replyMarkup)
                        .build()
        );
    }
}

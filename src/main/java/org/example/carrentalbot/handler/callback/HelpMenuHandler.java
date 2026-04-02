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
 * Displays the help menu with available commands and usage tips.
 * <p>
 * Available in all flow contexts. Sends an HTML-formatted guide
 * along with a navigation keyboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelpMenuHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "HELP";

    /**
     * Allowed flow contexts for this handler.
     * <p>Accessible from any flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Factory for constructing the help menu inline keyboard.
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
     * Sends help information and commands to the user.
     *
     * @param chatId chat identifier
     * @param callbackQuery incoming callback
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

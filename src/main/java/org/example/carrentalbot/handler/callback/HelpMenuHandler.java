package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class HelpMenuHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);
    public static final String KEY = "HELP";

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    public HelpMenuHandler(TelegramClient telegramClient,
                           KeyboardFactory keyboardFactory) {
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

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

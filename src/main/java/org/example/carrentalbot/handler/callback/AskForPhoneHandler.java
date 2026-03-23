package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service serves as the explicit prompt for user contact information.
 * It is responsible for:
 * <ul>
 * <li>Providing the unique {@code AskForPhoneHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Requesting a phone number from the user via a formatted text message.</li>
 * <li>Providing a clear example of the expected phone number format (international standard).</li>
 * <li>Dispatching an HTML-formatted message.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForPhoneHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code AskForPhoneHandler} and properly route callbacks.
     */
    public static final String KEY = "ASK_FOR_PHONE";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} because phone number collection
     * is a cross-cutting utility required in multiple flows.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages.
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
     * Executes the logic to prompt the user for their phone number.
     * <ol>
     * <li>Logs the initiation of the phone request flow.</li>
     * <li>Constructs an HTML-formatted message containing instructions and a format example.</li>
     * <li>Sends the message without an inline keyboard ({@code replyMarkup = null}) to
     * encourage the user to use the text input field.</li>
     * </ol>
     * @param chatId The ID of the chat where the prompt should be sent.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'ask for phone' flow");

        String text = """
                Please enter your phone.
                
                Example: +49 123 456789
                """;

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());
    }
}

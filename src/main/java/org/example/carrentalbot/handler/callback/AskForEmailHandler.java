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
 * <p>This service manages the transition to email collection within the booking or profile
 * update workflows. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code AskForEmailHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Prompting the user to provide a valid email address via a text message.</li>
 * <li>Providing a standardized example to guide the user toward correct input formatting.</li>
 * <li>Dispatching an HTML-formatted message.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForEmailHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code AskForEmailHandler} and properly route callbacks.
     */
    public static final String KEY = "ASK_FOR_EMAIL";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} as email collection is a shared
     * utility required in multiple flows.</p>
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
     * Executes the logic to prompt the user for their email address.
     * <ol>
     * <li>Logs the start of the email request sequence.</li>
     * <li>Constructs an HTML-formatted message containing instructions and a sample email format.</li>
     * <li>Dispatches the message with no inline keyboard ({@code replyMarkup = null}) to
     * encourage the user to use the text input field.</li>
     * </ol>
     * @param chatId The ID of the chat where the prompt should be sent.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'ask for mail' flow");

        String text = """
                Please enter your email address.

                Example: user@example.com
                """;

        log.debug("Building response message");
        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());

    }
}

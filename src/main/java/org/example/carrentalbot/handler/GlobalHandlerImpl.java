package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Default implementation of {@link GlobalHandler} acting as the central dispatcher
 * for all incoming Telegram updates.
 * <p>This component coordinates the full update processing pipeline by:
 * <ul>
 *   <li>Extracting the {@code chatId} from different update types</li>
 *   <li>Routing messages to command or text handlers</li>
 *   <li>Routing callback queries based on structured callback data prefixes</li>
 *   <li>Validating the user's current {@link FlowContext} before handler execution</li>
 * </ul>
 * <p>Handler resolution is delegated to {@link HandlerRegistry}, allowing
 * extensible and decoupled processing via command, text, and callback handlers.</p>
 * <p>Execution is performed asynchronously to avoid blocking the webhook thread.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalHandlerImpl implements GlobalHandler {

    /**
     * Client for interacting with the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * Validates whether a user is in the correct interaction flow context.
     */
    private final FlowContextHelper flowContextHelper;

    /**
     * Registry of command, text, and callback handlers with fallback support.
     */
    private final HandlerRegistry handlerRegistry;

    /**
     * Extracts the chat identifier from the given update.
     * <p>Supports both message and callback query updates. Returns {@code null}
     * if the chat cannot be determined.</p>
     *
     * @param update incoming Telegram update
     * @return chat identifier or {@code null} if unavailable
     */
    private Long extractChatId(UpdateDto update) {
        if (update.getMessage() != null) {
            return update.getMessage().getChat() != null && update.getMessage().getChat().getId() != null
                    ? update.getMessage().getChat().getId() : null;
        }
        if (update.getCallbackQuery() != null && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChat() != null && update.getCallbackQuery().getMessage().getChat().getId() != null
                    ? update.getCallbackQuery().getMessage().getChat().getId() : null;
        }
        return null;
    }

    /**
     * Entry point for processing a single Telegram update.
     * <p>Determines the update type and delegates processing to the appropriate
     * handler method. Unsupported update types are logged and ignored.</p>
     *
     * @param update non-null Telegram update payload
     */
    @Override
    @Async("telegramExecutor")
    public void handleUpdate(UpdateDto update) {

        if (update == null) {
            log.warn("Received null update");
            return;
        }

        Long chatId = extractChatId(update);

        if (update.getMessage() != null) {
            handleMessage(chatId, update.getMessage());
        } else if (update.getCallbackQuery() != null) {
            handleCallbackQuery(chatId, update.getCallbackQuery());
        } else {
            log.warn("Unhandled update: {}", update);
        }
    }

    /**
     * Processes an incoming message update.
     * <p>Routes the message based on its content:
     * <ul>
     *   <li>Commands (starting with {@code /}) are delegated to command handlers</li>
     *   <li>All other text is processed by registered text handlers</li>
     * </ul>
     * <p>Empty or malformed messages are ignored.</p>
     *
     * @param chatId identifier of the chat where the message originated
     * @param message incoming message payload
     */
    private void handleMessage(Long chatId, MessageDto message) {

        FromDto from = message.getFrom();
        if (message.getFrom() == null) {
            log.warn("Missing telegram user in message");
            return;
        }

        String text = Optional.ofNullable(message.getText()).map(String::trim).orElse("");
        if (text.isBlank()) {
            log.debug("Ignoring message with empty text");
            return;
        }

        if (text.startsWith("/")) {
            handleCommand(chatId, from, text);
        } else {
            handleText(chatId, text);
        }
    }

    /**
     * Resolves and executes a command handler.
     * <p>Performs a lookup in {@link HandlerRegistry} based on the normalized command text.
     * Falls back to a default handler if no match is found.</p>
     * <p>Before execution, the user's {@link FlowContext} is validated against
     * the handler's allowed contexts.</p>
     *
     * @param chatId chat identifier
     * @param from sender information
     * @param text raw command text (e.g. "/start")
     */
    private void handleCommand(Long chatId, FromDto from, String text) {
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        log.info("Executing command '{}'", text.toLowerCase());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, from);
    }

    /**
     * Resolves and executes a text handler.
     * <p>Selects the first {@link TextHandler} capable of handling the input.
     * If none match, a fallback handler is executed.</p>
     * <p>{@link FlowContext} validation is performed before invoking the selected handler.</p>
     *
     * @param chatId chat identifier
     * @param text message text content
     */
    private void handleText(Long chatId, String text) {
        handlerRegistry.getTextHandlers().stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            log.info("Executing text '{}'", text);

                            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                            handler.handle(chatId, text);
                        },
                        () -> {
                            FallbackTextHandler fallbackHandler = handlerRegistry.getFallbackTextHandler();

                            log.info("Executing text '{}'", text);
                            fallbackHandler.handle(chatId, text);
                        }
                );
    }

    /**
     * Processes an incoming callback query triggered by an inline keyboard interaction.
     * <p>Acknowledges the callback to remove the Telegram loading indicator.</p>
     * <p>Resolves a {@link CallbackHandler} based on callback data prefix matching
     * in {@link HandlerRegistry}. If no match is found, a fallback handler is used.</p>
     * <p>{@link FlowContext} validation is performed before invoking the handler.</p>
     *
     * @param chatId chat identifier
     * @param callbackQuery callback query payload
     */
    private void handleCallbackQuery(Long chatId, CallbackQueryDto callbackQuery) {

        String callbackData = Optional.ofNullable(callbackQuery.getData()).map(String::trim).orElse("");
        if (callbackData.isBlank()) {
            log.warn("Empty callback data: {}", callbackQuery);
            return;
        }

        telegramClient.answerCallbackQuery(
                AnswerCallbackQueryDto.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .text("Ok")
                        .showAlert(false)
                        .build());

        CallbackHandler handler = handlerRegistry.getCallbackHandlers().entrySet().stream()
                .filter(entry -> callbackData.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(handlerRegistry.getFallbackCallbackHandler());

        log.info("Executing callback '{}' ", callbackData);

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, callbackQuery);
    }
}
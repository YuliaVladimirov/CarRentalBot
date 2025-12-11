package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Central dispatcher for all incoming Telegram updates.
 *
 * <p>This service extracts the update type (message, command, text, callback)
 * and delegates it to the appropriate handler based on the registered
 * {@link CommandHandler}, {@link TextHandler}, and {@link CallbackHandler}
 * instances.</p>
 *
 * <p>The handler invocation respects conversational state by validating the
 * active flow context through {@link FlowContextHelper}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalHandlerImpl implements GlobalHandler {

    private final TelegramClient telegramClient;
    private final FlowContextHelper flowContextHelper;
    private final HandlerRegistry handlerRegistry;

    /**
     * Extracts the chat ID from a Telegram {@link UpdateDto}.
     *
     * <p>Chat ID is resolved either from the message or from the callback query's
     * message container. If neither exists, {@code null} is returned.</p>
     *
     * @param update the Telegram update
     * @return the chat ID if present, otherwise {@code null}
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
     * Entry point for processing any Telegram update.
     *
     * <p>The method resolves the update type (message or callback query)
     * and delegates further processing accordingly.</p>
     *
     * <p>Runs asynchronously on the {@code telegramExecutor} thread pool.</p>
     *
     * @param update the incoming Telegram update, may be {@code null}
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
     * Handles an incoming Telegram message.
     *
     * <p>Messages are classified into commands (starting with '/') or regular text,
     * and dispatched to the appropriate handler type.</p>
     *
     * @param chatId the chat ID where the message originated
     * @param message the Telegram message payload
     */
    @Override
    public void handleMessage(Long chatId, MessageDto message) {

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
     * Processes a bot command message.
     *
     * <p>Resolves the responsible {@link CommandHandler}. If no matching handler is found,
     * the fallback command handler is used.</p>
     *
     * <p>Ensures the command is permitted in the current flow context before execution.</p>
     *
     * @param chatId the originating chat ID
     * @param from the Telegram user issuing the command
     * @param text the command text, starting with '/'
     */
    private void handleCommand(Long chatId, FromDto from, String text) {
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        log.info("Executing command '{}'", text.toLowerCase());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, from);
    }

    /**
     * Processes a plain text message.
     *
     * <p>Finds the first {@link TextHandler} that claims it can handle the message.
     * If none match, the fallback text handler is used.</p>
     *
     * <p>Before handler execution, the user's active flow context is validated.</p>
     *
     * @param chatId the originating chat ID
     * @param text the non-command text message
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
     * Processes a callback query generated from an inline button.
     *
     * <p>Automatically sends a callback answer to Telegram confirming receipt, then
     * resolves and executes the appropriate {@link CallbackHandler} based on the
     * callback data prefix.</p>
     *
     * <p>If no matching handler is found, the fallback callback handler is used.</p>
     *
     * <p>Flow context is validated before handler execution.</p>
     *
     * @param chatId the originating chat ID
     * @param callbackQuery the callback query payload
     */
    @Override
    public void handleCallbackQuery(Long chatId, CallbackQueryDto callbackQuery) {

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
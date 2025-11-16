package org.example.carrentalbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.exception.TelegramExceptionHandler;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class TelegramService {

    private final TelegramClient telegramClient;
    private final FlowContextHelper flowContextHelper;

    private final Map<String, CallbackHandler> callbackHandlerMap;
    private final CallbackHandler fallbackCallbackHandler;

    private final Map<String, CommandHandler> commandHandlerMap;
    private final CommandHandler fallbackCommandHandler;

    private final List<TextHandler> textHandlerList;
    private final TextHandler fallbackTextHandler;

    private final TelegramExceptionHandler telegramExceptionHandler;

    public TelegramService(TelegramClient telegramClient,
                           FlowContextHelper flowContextHelper,
                           HandlerRegistry handlerRegistry,
                           TelegramExceptionHandler telegramExceptionHandler) {
        this.telegramClient = telegramClient;
        this.flowContextHelper = flowContextHelper;

        this.callbackHandlerMap = handlerRegistry.getCallbackHandlers();
        this.fallbackCallbackHandler = handlerRegistry.getFallbackCallbackHandler();

        this.commandHandlerMap = handlerRegistry.getCommandHandlers();
        this.fallbackCommandHandler = handlerRegistry.getFallbackCommandHandler();

        this.textHandlerList = handlerRegistry.getTextHandlers();
        this.fallbackTextHandler = handlerRegistry.getFallbackTextHandler();
        this.telegramExceptionHandler = telegramExceptionHandler;
    }

    @Async("telegramExecutor")
    public void handleUpdate(UpdateDto update) {
        if (update == null) {
            log.warn("Received null update");
            return;
        }
        try{
            if (update.getMessage() != null) {
                handleMessage(update.getMessage());
            } else if (update.getCallbackQuery() != null) {
                handleCallbackQuery(update.getCallbackQuery());
            } else {

                log.warn("Unhandled update: {}", update);
            }
        } catch (Exception exception) {
            telegramExceptionHandler.handleException(exception, update);
        }
    }

    public void handleMessage(MessageDto message) {
        if (message.getChat() == null) {
            log.warn("No chat id received in message: {}", message);
            return;
        }

        Long chatId = message.getChat().getId();
        String text = Optional.ofNullable(message.getText()).orElse("").trim();

        if (text.isEmpty()) {
            log.debug("Ignoring empty message from chatId {}", chatId);
            return;
        }

        if (text.startsWith("/")) {

            CommandHandler handler = commandHandlerMap
                    .getOrDefault(text.toLowerCase(), fallbackCommandHandler);

            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
            handler.handle(chatId, message);

            log.info("Executing command '{}' for chatId {}", text.toLowerCase(), chatId);
            return;
        }

        textHandlerList.stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                            handler.handle(chatId, message);
                        },
                        () -> fallbackTextHandler.handle(chatId, message)
                );
        log.info("Executing message for chatId {}: {}", chatId, text);
    }

    public void handleCallbackQuery(CallbackQueryDto callbackQuery) {
        if (callbackQuery.getMessage() == null || callbackQuery.getMessage().getChat() == null) {
            log.warn("Invalid callbackQuery received: {}", callbackQuery);
            return;
        }

        if (callbackQuery.getData() == null || callbackQuery.getData().isEmpty()) {
            log.warn("CallbackQuery data is null or empty: {}", callbackQuery);
            return;
        }

        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChat().getId();

        telegramClient.answerCallbackQuery(
                AnswerCallbackQueryDto.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .text("Ok")
                        .showAlert(false)
                        .build());

        CallbackHandler handler = callbackHandlerMap.entrySet().stream()
                .filter(entry -> callbackData.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(fallbackCallbackHandler);

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, callbackQuery);

        log.info("Executing callback '{}' for chatId {}", callbackData, chatId);
    }
}

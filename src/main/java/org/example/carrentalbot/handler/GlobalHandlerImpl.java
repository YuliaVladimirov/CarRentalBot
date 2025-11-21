package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.exception.TelegramExceptionHandler;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalHandlerImpl implements GlobalHandler {

    private final TelegramClient telegramClient;
    private final FlowContextHelper flowContextHelper;
    private final HandlerRegistry handlerRegistry;
    private final TelegramExceptionHandler telegramExceptionHandler;

    @Override
    @Async("telegramExecutor")
    public void handleUpdate(UpdateDto update) {

        if (update == null) {
            log.warn("Received null update");
            return;
        }

        try (MDC.MDCCloseable ignoredUpdateId = MDC.putCloseable("updateId", update.getUpdateId().toString())) {

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

    @Override
    public void handleMessage(MessageDto message) {
        if (message.getChat().getId() == null) {
            log.warn("Missing chatId in message: {}", message);
            return;
        }
        Long chatId = message.getChat().getId();

        if (message.getFrom() == null) {
            log.warn("Missing telegram user in message: {}", message);
            return;
        }
        FromDto from = message.getFrom();

        try (MDC.MDCCloseable ignoredChatId = MDC.putCloseable("chatId", chatId.toString());
             MDC.MDCCloseable ignoredUserId = MDC.putCloseable("userId", from.getId().toString())) {

            String text = Optional.ofNullable(message.getText()).orElse("").trim();
            if (text.isEmpty()) {
                log.debug("Ignoring empty text in message");
                return;
            }
            if (text.startsWith("/")) {
                handleCommand(chatId, from, text);
            } else {
                handleText(chatId, text);
            }
        }
    }

    private void handleCommand(Long chatId, FromDto from, String text) {
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        try (MDC.MDCCloseable ignoredHandler = MDC.putCloseable("handler", handler.getClass().getSimpleName())) {

            log.debug("Executing command '{}'", text.toLowerCase());

            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
            handler.handle(chatId, from);
        }
    }

    private void handleText(Long chatId, String text) {
        handlerRegistry.getTextHandlers().stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            try (MDC.MDCCloseable ignoredHandler = MDC.putCloseable("handler", handler.getClass().getSimpleName())) {

                                log.info("Executing text '{}'", text);

                                flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                                handler.handle(chatId, text);
                            }
                        },
                        () -> {
                            try (MDC.MDCCloseable ignoredHandler = MDC.putCloseable("handler", "FallbackTextHandler")) {
                                log.info("Executing text '{}'", text);
                                handlerRegistry.getFallbackTextHandler().handle(chatId, text);
                            }
                        }
                );
    }

    @Override
    public void handleCallbackQuery(CallbackQueryDto callbackQuery) {
        if (callbackQuery.getMessage() == null) {
            log.warn("Missing message in callback query: {}", callbackQuery);
            return;
        }

        if (callbackQuery.getMessage().getChat() == null || callbackQuery.getMessage().getChat().getId() == null) {
            log.warn("Missing chat or chatId in callback query: {}", callbackQuery);
            return;
        }
        Long chatId = callbackQuery.getMessage().getChat().getId();

        if (callbackQuery.getMessage().getFrom() == null) {
            log.warn("Missing telegram user in callback query: {}", callbackQuery);
            return;
        }
        FromDto from = callbackQuery.getMessage().getFrom();

        try (MDC.MDCCloseable ignoredChatId = MDC.putCloseable("chatId", chatId.toString());
             MDC.MDCCloseable ignoredUserId = MDC.putCloseable("userId", from.getId().toString())) {

            String callbackData = Optional.ofNullable(callbackQuery.getData()).orElse("").trim();
            if (callbackData.isEmpty()) {
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

            try (MDC.MDCCloseable ignoredHandler = MDC.putCloseable("handler", handler.getClass().getSimpleName())) {
                log.debug("Executing callback '{}' ", callbackData);

                flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                handler.handle(chatId, callbackQuery);
            }
        }
    }
}

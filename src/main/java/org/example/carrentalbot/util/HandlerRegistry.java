package org.example.carrentalbot.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central registry for all Telegram update handlers.
 *
 * <p>This component discovers and organizes all {@link CallbackHandler},
 * {@link CommandHandler}, and {@link TextHandler} implementations provided
 * via Spring dependency injection.</p>
 *
 * <p>Each handler category maintains a primary set of handlers along with a
 * mandatory fallback handler. The fallback is used when no dedicated handler
 * matches the incoming update.</p>
 *
 * <p>The registry is constructed once at application startup and remains immutable.</p>
 */
@Component
@Getter
@Slf4j
public class HandlerRegistry {

    private final Map<String, CallbackHandler> callbackHandlers;
    private final CallbackHandler fallbackCallbackHandler;

    private final Map<String, CommandHandler> commandHandlers;
    private final CommandHandler fallbackCommandHandler;

    private final List<TextHandler> textHandlers;
    private final FallbackTextHandler fallbackTextHandler;

    /**
     * Builds the handler registry by categorizing all provided handlers
     * into dedicated and fallback handler groups.
     *
     * <p>A handler is considered the fallback variant if its key/command is
     * {@code "__FALLBACK__"} (for callbacks and commands), or if it is an instance of
     * {@link FallbackTextHandler} (for text handlers).</p>
     *
     * <p>During initialization, each non-fallback handler is logged for visibility.</p>
     *
     * @param callbackHandlerList list of all callback handlers discovered by Spring
     * @param commandHandlerList  list of all command handlers discovered by Spring
     * @param textHandlerList     list of all text handlers discovered by Spring
     *
     * @throws IllegalStateException if any of the required fallback handlers
     *                               (callback, command, text) is missing
     */
    public HandlerRegistry(List<CallbackHandler> callbackHandlerList,
                           List<CommandHandler> commandHandlerList,
                           List<TextHandler> textHandlerList) {
        // Callbacks
        this.callbackHandlers = callbackHandlerList.stream()
                .filter(callbackHandler -> !"__FALLBACK__".equals(callbackHandler.getKey()))
                .peek(callbackHandler -> log.info("Registered callback handler: {}", callbackHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CallbackHandler::getKey, h -> h));

        this.fallbackCallbackHandler = callbackHandlerList.stream()
                .filter(callbackHandler -> "__FALLBACK__".equals(callbackHandler.getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback callback handler defined!"));

        // Commands
        this.commandHandlers = commandHandlerList.stream()
                .filter(commandHandler -> !"__FALLBACK__".equals(commandHandler.getCommand()))
                .peek(commandHandler -> log.info("Registered command handler: {}", commandHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CommandHandler::getCommand, h -> h));

        this.fallbackCommandHandler = commandHandlerList.stream()
                .filter(commandHandler -> "__FALLBACK__".equals(commandHandler.getCommand()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback command handler defined!"));

        // Text Handlers
        this.textHandlers = textHandlerList.stream()
                .filter(textHandler -> !(textHandler instanceof FallbackTextHandler))
                .peek(textHandler -> log.info("Registered text handler: {}", textHandler.getClass().getSimpleName()))
                .toList();

        this.fallbackTextHandler = textHandlerList.stream()
                .filter(textHandler -> textHandler instanceof FallbackTextHandler)
                .map(textHandler -> (FallbackTextHandler) textHandler)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback text handler defined!"));
    }
}

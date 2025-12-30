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
 * Registry component that manages and distributes different types of message handlers.
 * <p>
 * This class acts as a central repository for {@link CallbackHandler}, {@link CommandHandler},
 * and {@link TextHandler} implementations. It categorizes handlers into maps for quick lookup
 * and identifies mandatory fallback handlers for each category.
 * </p>
 */
@Component
@Getter
@Slf4j
public class HandlerRegistry {

    /**
     * Map of callback handlers indexed by their unique key.
     */
    private final Map<String, CallbackHandler> callbackHandlers;

    /**
     * The default handler used when no specific callback key matches.
     */
    private final CallbackHandler fallbackCallbackHandler;

    /**
     * Map of command handlers indexed by their specific command string.
     */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * The default handler used when an unknown command is received.
     */
    private final CommandHandler fallbackCommandHandler;

    /**
     * List of handlers processed sequentially for plain text messages.
     */
    private final List<TextHandler> textHandlers;

    /**
     * The default handler used when no text handlers can process the input.
     */
    private final FallbackTextHandler fallbackTextHandler;

    /**
     * Constructs the registry by filtering and indexing provided handler implementations.
     * <p>
     * Handlers marked with the key {@code "__FALLBACK__"} are assigned as fallback handlers.
     * All other handlers are registered into their respective maps or lists.
     * </p>
     * @param callbackHandlerList List of available {@link CallbackHandler} beans.
     * @param commandHandlerList  List of available {@link CommandHandler} beans.
     * @param textHandlerList     List of available {@link TextHandler} beans.
     * @throws IllegalStateException if a required fallback handler is missing from the provided lists.
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

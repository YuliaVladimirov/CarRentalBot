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
 * Central registry for all handler implementations used in update processing.
 * <p>Aggregates and organizes {@link CallbackHandler}, {@link CommandHandler},
 * and {@link TextHandler} beans, providing efficient access patterns for each type:</p>
 * <ul>
 *   <li><b>Callback handlers</b> — indexed by key for prefix-based resolution</li>
 *   <li><b>Command handlers</b> — indexed by command for direct lookup</li>
 *   <li><b>Text handlers</b> — evaluated sequentially using {@code canHandle}</li>
 * </ul>
 * <p>Each handler category defines a mandatory fallback handler that is used
 * when no specific handler matches the input.</p>
 * <p>This component is initialized at application startup by collecting all
 * handler beans from the Spring context.</p>
 */
@Component
@Getter
@Slf4j
public class HandlerRegistry {

    public static final String FALLBACK_KEY = "__FALLBACK__";

    /**
     * Callback handlers indexed by their unique key.
     */
    private final Map<String, CallbackHandler> callbackHandlers;

    /**
     * Fallback handler used when no callback key matches.
     */
    private final CallbackHandler fallbackCallbackHandler;

    /**
     * Command handlers indexed by their command string (e.g. "/start").
     */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * Fallback handler used when an unknown command is received.
     */
    private final CommandHandler fallbackCommandHandler;

    /**
     * Ordered list of text handlers evaluated sequentially.
     */
    private final List<TextHandler> textHandlers;

    /**
     * Fallback handler used when no text handler can process the input.
     */
    private final FallbackTextHandler fallbackTextHandler;

    /**
     * Constructs the registry by indexing and categorizing handler implementations.
     * <p>Handlers are discovered via Spring injection and grouped as follows:</p>
     * <ul>
     *   <li>Callback and command handlers are indexed into maps for fast lookup</li>
     *   <li>Text handlers are stored as an ordered list for sequential evaluation</li>
     * </ul>
     * <p>Fallback handlers are identified using a reserved key ({@link HandlerRegistry#FALLBACK_KEY})
     * for callback and command handlers, and by type ({@link FallbackTextHandler})
     * for text handlers.</p>
     *
     * @param callbackHandlerList all available {@link CallbackHandler} beans
     * @param commandHandlerList  all available {@link CommandHandler} beans
     * @param textHandlerList     all available {@link TextHandler} beans
     *
     * @throws IllegalStateException if any required fallback handler is missing
     */
    public HandlerRegistry(List<CallbackHandler> callbackHandlerList,
                           List<CommandHandler> commandHandlerList,
                           List<TextHandler> textHandlerList) {
        // Callbacks
        this.callbackHandlers = callbackHandlerList.stream()
                .filter(callbackHandler -> !FALLBACK_KEY.equals(callbackHandler.getKey()))
                .peek(callbackHandler -> log.info("Registered callback handler: {}", callbackHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CallbackHandler::getKey, h -> h));

        this.fallbackCallbackHandler = callbackHandlerList.stream()
                .filter(callbackHandler -> FALLBACK_KEY.equals(callbackHandler.getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback callback handler defined!"));

        // Commands
        this.commandHandlers = commandHandlerList.stream()
                .filter(commandHandler -> !FALLBACK_KEY.equals(commandHandler.getCommand()))
                .peek(commandHandler -> log.info("Registered command handler: {}", commandHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CommandHandler::getCommand, h -> h));

        this.fallbackCommandHandler = commandHandlerList.stream()
                .filter(commandHandler -> FALLBACK_KEY.equals(commandHandler.getCommand()))
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

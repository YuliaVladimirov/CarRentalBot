package org.example.carrentalbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.config.TelegramBotProperties;
import org.example.carrentalbot.handler.GlobalHandler;
import org.example.carrentalbot.dto.UpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that receives and processes updates from the Telegram Bot webhook.
 * <p>This is the single entry point for all incoming Telegram events, including
 * messages, commands, and callback queries.</p>
 * <p>Requests are optionally validated using the
 * {@code X-Telegram-Bot-Api-Secret-Token} header to ensure they originate from
 * Telegram.</p>
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    /**
     * Delegates incoming updates to the appropriate handler based on their type
     * (messages, commands, callback queries, etc.).
     */
    private final GlobalHandler globalHandler;

    /**
     * Telegram bot configuration properties, including the optional webhook secret
     * used for request validation.
     */
    private final TelegramBotProperties telegramBotProperties;

    /**
     * Handles incoming webhook updates from Telegram.
     * <p>Validates the optional secret token (if configured) and rejects requests
     * that do not match the expected value.</p>
     * <p>Valid updates are forwarded to the {@link GlobalHandler} for processing.</p>
     *
     * @param secretHeader value of the {@code X-Telegram-Bot-Api-Secret-Token} header
     * @param update incoming Telegram update payload
     * @return HTTP 200 if processed successfully, or HTTP 403 if the secret token is invalid
     */
    @PostMapping
    public ResponseEntity<String> onUpdate(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretHeader,
            @RequestBody UpdateDto update) {

        if (telegramBotProperties.secret() != null && !telegramBotProperties.secret().isBlank()) {
            if (!telegramBotProperties.secret().equals(secretHeader)) {
                log.warn("updateId={} Rejected update due to invalid secret header", update.getUpdateId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid secret");
            }
        }

        log.debug("Received webhook update (id: {})", update.getUpdateId());

        globalHandler.handleUpdate(update);
        return ResponseEntity.ok("OK");
    }
}

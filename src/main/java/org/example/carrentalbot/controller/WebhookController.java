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
 * Controller responsible for receiving and processing updates from the Telegram Bot API
 * via a webhook mechanism.
 * <p>It serves as the single entry point for all incoming messages, commands, and
 * callback queries from Telegram users.</p>
 * <p>This controller implements a security check using the {@code X-Telegram-Bot-Api-Secret-Token}
 * header to ensure that updates originate from the trusted Telegram server.</p>
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    /**
     * The core handler component responsible for parsing the {@link UpdateDto}
     * and routing it to the appropriate business logic (e.g., command,
     * message or callback handlers, etc.).
     */
    private final GlobalHandler globalHandler;

    /**
     * Configuration properties related to the Telegram bot, primarily used here
     * to access the required secret token for webhook validation.
     */
    private final TelegramBotProperties telegramBotProperties;

    /**
     * Main handler method that receives and processes incoming updates from the Telegram Bot API.
     * <p>It first validates the optional secret token provided by Telegram to secure
     * the webhook endpoint. If the secret is configured and invalid, the request
     * is rejected with a 403 Forbidden status.</p>
     * @param secretHeader The security token provided by Telegram in the {@code X-Telegram-Bot-Api-Secret-Token} header.
     * @param update The deserialized body of the incoming update from Telegram.
     * @return A {@link ResponseEntity} with status 200 (OK) if processed successfully,
     * or 403 (FORBIDDEN) if the secret token is invalid.
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

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
 * REST controller that handles incoming Telegram webhook updates.
 *
 * <p>This endpoint is called directly by Telegram when the bot receives new updates.
 * It optionally validates the request using the Telegram secret token header
 * (X-Telegram-Bot-Api-Secret-Token) if configured.</p>
 *
 * <p>All valid updates are delegated to {@link GlobalHandler} for processing.</p>
 */

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final GlobalHandler globalHandler;
    private final TelegramBotProperties telegramBotProperties;

    /**
     * Handles an incoming Telegram webhook update.
     *
     * @param secretHeader optional secret token supplied by Telegram in header
     *                     {@code X-Telegram-Bot-Api-Secret-Token}.
     *                     Used for validating the authenticity of the request.
     * @param update the webhook payload sent by Telegram containing update details
     * @return {@code 200 OK} if the update is valid and processed successfully,
     *         or {@code 403 Forbidden} if the secret token validation fails
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

package org.example.carrentalbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.config.TelegramBotProperties;
import org.example.carrentalbot.handler.GlobalHandlerImpl;
import org.example.carrentalbot.dto.UpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final GlobalHandlerImpl globalHandler;
    private final TelegramBotProperties telegramBotProperties;

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

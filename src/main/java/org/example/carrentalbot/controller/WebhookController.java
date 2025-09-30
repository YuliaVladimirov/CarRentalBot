package org.example.carrentalbot.controller;

import org.example.carrentalbot.config.TelegramBotProperties;
import org.example.carrentalbot.service.TelegramService;
import org.example.carrentalbot.dto.UpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final TelegramService telegramService;
    private final TelegramBotProperties telegramBotProperties;

    public WebhookController(TelegramService telegramService, TelegramBotProperties telegramBotProperties) {
        this.telegramService = telegramService;
        this.telegramBotProperties = telegramBotProperties;

    }

    @PostMapping
    public ResponseEntity<String> onUpdate(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretHeader,
            @RequestBody UpdateDto update) {

        if (telegramBotProperties.secret() != null && !telegramBotProperties.secret().isBlank()) {
            if (!telegramBotProperties.secret().equals(secretHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid secret");
            }
        }
        telegramService.handleUpdate(update);
        return ResponseEntity.ok("OK");
    }
}

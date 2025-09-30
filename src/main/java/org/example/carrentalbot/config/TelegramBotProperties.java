package org.example.carrentalbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(String token, String username, String secret, String webhookUrl) {}

package org.example.carrentalbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Telegram bot integration.
 * <p>Maps values from the application configuration with prefix
 * {@code telegram.bot} into a strongly-typed record.</p>
 * <p>Used to configure authentication, bot identity, and webhook settings
 * required for interacting with the Telegram Bot API.</p>
 *
 * @param token Telegram bot API token used for authentication
 * @param username Telegram bot username (without '@')
 * @param secret optional secret token used for webhook validation
 * @param webhookUrl public webhook URL used for receiving updates
 */
@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(String token, String username, String secret, String webhookUrl) {}

package org.example.carrentalbot.util;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.config.TelegramBotProperties;
import org.example.carrentalbot.dto.AnswerCallbackQueryDto;
import org.example.carrentalbot.dto.EditMessageReplyMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.dto.SendPhotoDto;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Low-level client responsible for communicating with the Telegram Bot API.
 * <p>Encapsulates HTTP calls to Telegram endpoints and provides asynchronous,
 * retry-enabled methods for sending messages, photos, and updating message markup.</p>
 * <p>All requests are executed through {@link RestTemplate} and routed via the bot's
 * base API URL derived from configuration properties.</p>
 */
@Slf4j
@Component
public class TelegramClient {

    private final RestTemplate restTemplate;
    private final String apiBase;

    /**
     * Creates a new Telegram API client using the provided REST template and bot configuration.
     *
     * @param restTemplate HTTP client used to execute requests
     * @param telegramBotProperties configuration properties containing the bot token
     */
    public TelegramClient(RestTemplate restTemplate,
                          TelegramBotProperties telegramBotProperties) {
        this.restTemplate = restTemplate;
        this.apiBase = "https://api.telegram.org/bot" + telegramBotProperties.token();
    }

    /**
     * Builds a full Telegram API endpoint URL for the given method.
     *
     * @param method Telegram API method name (e.g., "sendMessage", "sendPhoto")
     * @return full API URL
     */
    private String getApiUrl(String method) {
        return apiBase + "/" + method;
    }

    /**
     * Sends a response to a callback query triggered by an inline keyboard interaction.
     * <p>Used to acknowledge user actions and optionally display notifications.</p>
     *
     * @param request callback query response payload
     */
    public void answerCallbackQuery(AnswerCallbackQueryDto request) {
        restTemplate.postForEntity(
                getApiUrl("answerCallbackQuery"),
                request,
                String.class
        );
    }

    /**
     * Sends a text message to a Telegram chat.
     * <p>Executed asynchronously with retry support for transient network failures.
     * If all retry attempts fail, the failure is handled by a recovery method.</p>
     *
     * @param request message payload
     */
    @Async("telegramExecutor")
    @Retryable(
            retryFor = {ResourceAccessException.class, IOException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000),
            recover = "recoverFailedMessage")
    public void sendMessage(SendMessageDto request) {
        restTemplate.postForEntity(
                getApiUrl("sendMessage"),
                request,
                String.class);

        log.info("Successfully sent message to chat id: {}", request.getChatId());
    }

    /**
     * Handles permanent failure when a message cannot be delivered after retries.
     *
     * @param exception the last encountered exception
     * @param dto original message request that failed
     */
    @Recover
    public void recoverFailedMessage(Exception exception, SendMessageDto dto) {
        log.error("PERMANENTLY FAILED to send Telegram message to {}: {}",
                dto.getChatId(),
                exception.getMessage());
    }

    /**
     * Sends a photo message to a Telegram chat.
     * <p>Supports asynchronous execution and automatic retries on temporary failures.</p>
     *
     * @param request photo message payload
     */
    @Async("telegramExecutor")
    @Retryable(
            retryFor = { ResourceAccessException.class, IOException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000),
            recover = "recoverFailedSendPhoto")
    public void sendPhoto(SendPhotoDto request) {
        restTemplate.postForEntity(
                getApiUrl("sendPhoto"),
                request,
                String.class);

        log.info("Successfully sent photo to chat id: {}", request.getChatId());
    }

    /**
     * Handles permanent failure when a photo message cannot be delivered after retries.
     *
     * @param exception the last encountered exception
     * @param dto original photo request that failed
     */
    @Recover
    public void recoverFailedSendPhoto(Exception exception, SendPhotoDto dto) {
        log.error("PERMANENTLY FAILED to send Telegram photo to {}: {}",
                dto.getChatId(),
                exception.getMessage());
    }

    /**
     * Updates the inline keyboard of an existing Telegram message.
     * <p>Used for dynamic UI updates such as pagination, calendar navigation,
     * or state changes in interactive messages.</p>
     *
     * @param request reply markup update payload
     */
    @Async("telegramExecutor")
    @Retryable(
            retryFor = {ResourceAccessException.class, IOException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 2000),
            recover = "recoverFailedEditMessageMarkup")
    public void sendEditMessageReplyMarkup(EditMessageReplyMarkupDto request) {

        restTemplate.postForEntity(
                getApiUrl("editMessageReplyMarkup"),
                request,
                String.class
        );

        log.info("Successfully edited reply markup for message {} in chat {}",
                request.getMessageId(), request.getChatId());
    }

    /**
     * Handles permanent failure when message reply markup cannot be updated after retries.
     *
     * @param exception the last encountered exception
     * @param dto original request that failed
     */
    @Recover
    public void recoverFailedEditMessageMarkup(Exception exception, EditMessageReplyMarkupDto dto) {
        log.error("PERMANENTLY FAILED to edit reply markup for message {} in chat {}: {}",
                dto.getMessageId(),
                dto.getChatId(),
                exception.getMessage());
    }
}

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

@Slf4j
@Component
public class TelegramClient {

    private final RestTemplate restTemplate;
    private final String apiBase;

    public TelegramClient(RestTemplate restTemplate,
                          TelegramBotProperties telegramBotProperties) {
        this.restTemplate = restTemplate;
        this.apiBase = "https://api.telegram.org/bot" + telegramBotProperties.token();
    }

    private String getApiUrl(String method) {
        return apiBase + "/" + method;
    }

    public void answerCallbackQuery(AnswerCallbackQueryDto request) {
        restTemplate.postForEntity(
                getApiUrl("answerCallbackQuery"),
                request,
                String.class
        );
    }

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

    @Recover
    public void recoverFailedMessage(Exception exception, SendMessageDto dto) {
        log.error("PERMANENTLY FAILED to send Telegram message to {}: {}",
                dto.getChatId(),
                exception.getMessage());
    }

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

    @Recover
    public void recoverFailedSendPhoto(Exception exception, SendMessageDto dto) {
        log.error("PERMANENTLY FAILED to send Telegram photo to {}: {}",
                dto.getChatId(),
                exception.getMessage());
    }

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

    @Recover
    public void recoverFailedEditMessageMarkup(Exception exception, EditMessageReplyMarkupDto dto) {
        log.error("PERMANENTLY FAILED to edit reply markup for message {} in chat {}: {}",
                dto.getMessageId(),
                dto.getChatId(),
                exception.getMessage());
    }
}

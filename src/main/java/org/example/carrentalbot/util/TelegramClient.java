package org.example.carrentalbot.util;

import org.example.carrentalbot.config.TelegramBotProperties;
import org.example.carrentalbot.dto.AnswerCallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TelegramClient {

    private final RestTemplate restTemplate;
    private final String apiBase;

    public TelegramClient(RestTemplate restTemplate, TelegramBotProperties telegramBotProperties) {
        this.restTemplate = restTemplate;
        this.apiBase = "https://api.telegram.org/bot" + telegramBotProperties.token();
    }

    private String getApiUrl(String method) {
        return apiBase + "/" + method;
    }

    public void sendMessage(SendMessageDto request) {
         restTemplate.postForEntity(
                getApiUrl("sendMessage"),
                request,
                String.class);
    }

    public void answerCallbackQuery(AnswerCallbackQueryDto request) {
        restTemplate.postForEntity(
                getApiUrl("answerCallbackQuery"),
                request,
                String.class
        );
    }

}

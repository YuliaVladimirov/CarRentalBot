package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Component
public class RentalDatesTextHandler implements TextHandler {

    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s*-\\s*\\d{2}\\.\\d{2}\\.\\d{4}");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public RentalDatesTextHandler(SessionService sessionService, KeyboardFactory keyboardFactory, TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public boolean canHandle(String text) {
        return text != null && DATE_RANGE_PATTERN.matcher(text.trim()).matches();
    }

    @Override
    public void handle(Long chatId, MessageDto message) {
        String[] parts = message.getText().split("-");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate startDate = LocalDate.parse(parts[0].trim(), formatter);
        LocalDate endDate = LocalDate.parse(parts[1].trim(), formatter);

        sessionService.put(chatId, "startDate", startDate);
        sessionService.put(chatId, "endDate", endDate);

        String text = String.format("""
    ✅ You entered:
    
    Rental period: %s - %s

    Please confirm or enter again.
    """, startDate.format(formatter), endDate.format(formatter));

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildConfirmRentalDatesKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build());

    }


}

package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class ConfirmRentalDatesHandler implements TextHandler {

    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s*-\\s*\\d{2}\\.\\d{2}\\.\\d{4}");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public ConfirmRentalDatesHandler(SessionService sessionService, KeyboardFactory keyboardFactory, TelegramClient telegramClient) {
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

        LocalDate[] rentalDates = retrieveRentalDates(chatId, message.getText());
        LocalDate startDate = rentalDates[0];
        LocalDate endDate = rentalDates[1];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String text = String.format("""
                You entered:
                Rental period: %s - %s

                Please confirm or enter again.
                """, startDate.format(formatter), endDate.format(formatter));

        CarBrowsingMode carBrowsingMode = sessionService.get(chatId, "carBrowsingMode", CarBrowsingMode.class).orElseThrow(() -> new DataNotFoundException(chatId, "Data not found"));

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildConfirmRentalDatesKeyboard(carBrowsingMode);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build());

    }

    private LocalDate[] retrieveRentalDates(Long chatId, String text) {

        LocalDate[] datesFromCallback = extractDatesFromMessageText(chatId, text);

        LocalDate startDateFromSession = sessionService.get(chatId, "startDate", LocalDate.class).orElse(null);
        LocalDate endDateFromSession = sessionService.get(chatId, "endDate", LocalDate.class).orElse(null);

        if (datesFromCallback == null && (startDateFromSession == null || endDateFromSession == null)) {
            throw new DataNotFoundException(chatId, "❌ Rental dates not found in callback or session");
        }

        LocalDate startDate = (datesFromCallback != null) ? datesFromCallback[0] : startDateFromSession;
        LocalDate endDate = (datesFromCallback != null) ? datesFromCallback[1] : endDateFromSession;

        if (datesFromCallback != null && (!startDate.equals(startDateFromSession) || !endDate.equals(endDateFromSession))) {
            sessionService.put(chatId, "startDate", startDate);
            sessionService.put(chatId, "endDate", endDate);
        }


        return new LocalDate[]{startDate, endDate};
    }

    private LocalDate[] extractDatesFromMessageText(Long chatId, String text) {

        return Optional.ofNullable(text)
                .map(datePart -> Arrays.stream(datePart.split("-"))
                        .map(String::trim)
                        .map(dateStr -> {
                            try {
                                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                            } catch (DateTimeParseException e) {
                                throw new InvalidDataException(chatId, "❌ Invalid date format: " + datePart);
                            }
                        })
                        .toArray(LocalDate[]::new)
                )
                .filter(array -> array.length == 2)
                .orElse(null);
    }
}

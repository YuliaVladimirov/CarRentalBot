package org.example.carrentalbot.handler.text;

import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.MessageDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.handler.callback.BrowseCarsForDatesHandler;
import org.example.carrentalbot.handler.callback.CheckCarAvailabilityHandler;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class ConfirmRentalDatesHandler implements TextHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);
    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s*-\\s*\\d{2}\\.\\d{2}\\.\\d{4}");

    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public ConfirmRentalDatesHandler(SessionService sessionService,
                                     KeyboardFactory keyboardFactory,
                                     TelegramClient telegramClient) {
        this.sessionService = sessionService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }

    @Override
    public boolean canHandle(String text) {
        return text != null && DATE_RANGE_PATTERN.matcher(text.trim()).matches();
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, MessageDto message) {

        LocalDate[] rentalDates = extractDatesFromMessageText(message.getText());

        LocalDate startDate = rentalDates[0];
        LocalDate endDate = rentalDates[1];

        sessionService.put(chatId, "startDate", startDate);
        sessionService.put(chatId, "endDate", endDate);

        String text;
        InlineKeyboardMarkupDto replyMarkup;
        String callbackKey = getDataForKeyboard(chatId);

        if (validateRentalDates(startDate, endDate)) {

            text = String.format("""
                    Confirm your rental dates:
                    <b>%s - %s</b>
                
                    Press <b>OK</b> to continue
                     or enter new dates.
                    """, startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

            replyMarkup = keyboardFactory.buildOkKeyboard(callbackKey);

        } else {
            text = """
                    <b>Invalid rental period:</b>
                    
                    ⚠️ <b>Make sure:</b>
                    • You cannot book for past days
                    • The start date must be before the end date.
                    
                    Please check your dates and re-enter:
                    """;
            replyMarkup = keyboardFactory.buildToMainMenuKeyboard();
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    private boolean validateRentalDates(LocalDate startDate, LocalDate endDate) {
        return !startDate.isBefore(LocalDate.now()) && !endDate.isBefore(LocalDate.now()) && !startDate.isAfter(endDate);
    }

    private LocalDate[] extractDatesFromMessageText(String text) {

        return Optional.ofNullable(text)
                .map(datePart -> Arrays.stream(datePart.split("-"))
                        .map(String::trim)
                        .map(dateStr -> {
                            try {
                                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                            } catch (DateTimeParseException e) {
                                throw new InvalidDataException("Invalid date format: " + datePart);
                            }
                        })
                        .toArray(LocalDate[]::new)
                )
                .filter(array -> array.length == 2)
                .orElse(null);
    }

    private String getDataForKeyboard(Long chatId) {
        CarBrowsingMode carBrowsingMode = sessionService
                .getCarBrowsingMode(chatId, "carBrowsingMode")
                .orElseThrow(() -> new DataNotFoundException("Car browsing mode not found in session"));

        return switch (carBrowsingMode) {
            case ALL_CARS -> CheckCarAvailabilityHandler.KEY;
            case CARS_FOR_DATES -> BrowseCarsForDatesHandler.KEY;
        };
    }
}

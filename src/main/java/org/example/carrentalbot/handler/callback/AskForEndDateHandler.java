package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.EditMessageReplyMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.InvalidDataException;
import org.example.carrentalbot.model.enums.CalendarAction;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class AskForEndDateHandler implements CallbackHandler {

    public static final String KEY = "END_DATE";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final KeyboardFactory keyboardFactory;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        InlineKeyboardMarkupDto replyMarkup;

        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        String[] callbackParts = data.split(":");
        CalendarAction action;

        try {
            action = CalendarAction.valueOf(callbackParts[1]);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid calendar action: " + callbackParts[1]);
        }

        switch (action) {

            case IGNORE -> { return; }

            case PREV, NEXT -> {
                replyMarkup = handleMonthChange(callbackParts);

                telegramClient.sendEditMessageReplyMarkup(EditMessageReplyMarkupDto.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .replyMarkup(replyMarkup)
                        .build());
            }

            case PICK -> {
                LocalDate startDate = LocalDate.parse(callbackParts[2]);

                String text;

                if (validateStartDate(startDate)) {

                    sessionService.put(chatId, "startDate", startDate);

                    YearMonth yearMonth = YearMonth.from(startDate);

                    text = String.format("""
                            Start date selected: <b>%s</b>
                            Now pick the end date:
                            """, startDate);

                    replyMarkup = keyboardFactory.buildCalendar(yearMonth.getYear(), yearMonth.getMonthValue(), ConfirmDatesHandler.KEY + ":");

                } else {

                    text = """
                            <b>Invalid start date:</b>
                            
                            ⚠️ <b>Make sure:</b>
                            • You cannot book for past days
                            • Start date must NOT be before the current date.
                            
                            Please check your date and re-enter:
                            """;

                    replyMarkup = keyboardFactory.buildInvalidDatesKeyboard();
                }

                telegramClient.sendMessage(SendMessageDto.builder()
                        .chatId(chatId.toString())
                        .text(text)
                        .parseMode("HTML")
                        .replyMarkup(replyMarkup)
                        .build());
            }
        }
    }

    private InlineKeyboardMarkupDto handleMonthChange(String[] p) {
        int year = Integer.parseInt(p[2]);
        int month = Integer.parseInt(p[3]);

        if (p[1].equals("PREV")) {
            month--;
            if (month == 0) {
                month = 12;
                year--;
            }
        } else {
            month++;
            if (month == 13) {
                month = 1;
                year++;
            }
        }

        return keyboardFactory.buildCalendar(year, month, AskForEndDateHandler.KEY + ":");
    }

    public boolean validateStartDate(LocalDate startDate) {
        return !startDate.isBefore(LocalDate.now());
    }
}

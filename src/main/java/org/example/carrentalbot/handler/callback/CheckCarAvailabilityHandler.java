package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingServiceImpl;
import org.example.carrentalbot.session.SessionServiceImpl;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckCarAvailabilityHandler implements CallbackHandler {

    public static final String KEY = "CHECK_AVAILABILITY";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final BookingServiceImpl bookingService;
    private final SessionServiceImpl sessionService;
    private final KeyboardFactory keyboardFactory;
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
        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));

        boolean available = bookingService.isCarAvailable(carId, startDate, endDate);

        String carAvailable = """
                This car is <b>available</b> for your selected dates!
                
                You can proceed to booking.
                """;

        String carUnavailable = """
                Sorry, this car is <b>not available</b> for the selected dates.
                
                Please choose different dates or another car.
                """;

        String text = available ? carAvailable : carUnavailable;

        InlineKeyboardMarkupDto replyMarkup = available ? keyboardFactory.buildCarAvailableKeyboard() : keyboardFactory.buildCarUnavailableKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

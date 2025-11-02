package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Component
public class CheckCarAvailabilityHandler implements CallbackHandler {

    public static final String KEY = "CHECK_AVAILABILITY";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public CheckCarAvailabilityHandler(BookingService bookingService,
                                       SessionService sessionService,
                                       NavigationService navigationService,
                                       KeyboardFactory keyboardFactory,
                                       TelegramClient telegramClient) {
        this.bookingService = bookingService;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.keyboardFactory = keyboardFactory;
        this.telegramClient = telegramClient;
    }


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

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

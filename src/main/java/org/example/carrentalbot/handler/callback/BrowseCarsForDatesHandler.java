package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Component
public class BrowseCarsForDatesHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CARS_FOR_DATES";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    private final CarService carService;
    private final NavigationService navigationService;
    private final SessionService sessionService;
    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

    public BrowseCarsForDatesHandler(CarService carService,
                                     NavigationService navigationService,
                                     SessionService sessionService, KeyboardFactory keyboardFactory,
                                     TelegramClient telegramClient) {
        this.carService = carService;
        this.navigationService = navigationService;
        this.sessionService = sessionService;
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

        CarCategory carCategory = sessionService
                .get(chatId, "carCategory", CarCategory.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Category not found in session"));

        LocalDate startDate = sessionService
                .get(chatId, "startDate", LocalDate.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "Start date not found in session"));

        LocalDate endDate = sessionService
                .get(chatId, "endDate", LocalDate.class)
                .orElseThrow(() -> new DataNotFoundException(chatId, "End date not found in session"));

        List<Car> availableCars = carService.getAvailableCarsByCategoryAndDates(carCategory, startDate, endDate);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCarsKeyboard(availableCars);

        String text = String.format("""
                        <b>Available cars in category '%s':</b>
                        """, carCategory);

        navigationService.push(chatId, KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}

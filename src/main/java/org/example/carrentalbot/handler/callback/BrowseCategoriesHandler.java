package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseCategoriesHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CATEGORIES";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final CarService carService;
    private final SessionService sessionService;
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
        log.info("Processing 'browse car categories' flow");

        sessionService.put(chatId, "flowContext", FlowContext.BROWSING_FLOW);
        log.debug("Session updated: 'flowContext' set to {}", FlowContext.BROWSING_FLOW);

        List<CarProjection> carCategories = carService.getCarCategories();
        log.info("Fetched {} car categories", carCategories.size());


        InlineKeyboardMarkupDto keyboard = keyboardFactory.buildCarCategoryKeyboard(carCategories);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Available Categories:</b>")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
    }
}

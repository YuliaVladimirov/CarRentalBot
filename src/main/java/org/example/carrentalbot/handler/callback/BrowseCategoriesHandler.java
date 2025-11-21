package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.CarServiceImpl;
import org.example.carrentalbot.session.SessionServiceImpl;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BrowseCategoriesHandler implements CallbackHandler {

    public static final String KEY = "BROWSE_CATEGORIES";
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    private final CarServiceImpl carService;
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
        sessionService.put(chatId, "flowContext", FlowContext.BROWSING_FLOW);

        List<CarProjectionDto> carCategories = carService.getCarCategories();
        InlineKeyboardMarkupDto keyboard = keyboardFactory.buildCarCategoryKeyboard(carCategories);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("<b>Available Categories:</b>")
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build());
    }
}

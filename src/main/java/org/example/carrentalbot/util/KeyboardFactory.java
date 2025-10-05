package org.example.carrentalbot.util;

import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardButtonDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkupDto buildMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🚗 Browse Categories")
                                .callbackData("BROWSE_CATEGORIES")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📒 My Bookings")
                                .callbackData("MY_BOOKINGS")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🧑 My Profile")
                                .callbackData("MY_PROFILE")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📞 Help")
                                .callbackData("HELP")
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarCategoryKeyboard(List<CarProjectionDto> availability) {
        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (CarProjectionDto dto : availability) {
            String emoji = getCategoryEmoji(dto.category());
            BigDecimal minimalDailyRate = dto.minimalDailyRate().setScale(0, RoundingMode.HALF_UP);

            InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                    .text(String.format("%s %s - from €%s/day", emoji, dto.category().getValue(), minimalDailyRate))
                    .callbackData("BROWSE_CARS:" + dto.category().name())
                    .build();

            rows.add(List.of(button));
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData("GO_BACK")
                .build();

        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    private String getCategoryEmoji(CarCategory category) {
        return switch (category) {
            case SEDAN -> "🚗";
            case SUV -> "🚌";
            case HATCHBACK -> "🚙";
            case CONVERTIBLE -> "🏎️";
            case VAN -> "🚐";
        };
    }

    public InlineKeyboardMarkupDto buildCarKeyboard(List<Car> cars) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (Car car : cars) {
            InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                    .text(String.format("%s  %s (%s)", "🔸", car.getBrand(), car.getModel()))
                    .callbackData("CAR_DETAILS:" + car.getId())
                    .build();

            rows.add(List.of(button));
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData("GO_BACK")
                .build();

        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(Car car) {
        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("BOOK")
                .callbackData("BOOK_CAR:" + car.getId())
                .build();

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData("GO_BACK")
                .build();
        rows.add(List.of(button, backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }
}

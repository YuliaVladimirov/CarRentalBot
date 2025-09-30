package org.example.carrentalbot.util;

import org.example.carrentalbot.dto.CategoryAvailabilityDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardButtonDto;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkupDto buildMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🚗 Browse Categories")
                                .callbackData("BROWSE_CATEGORIES:")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📒 My Bookings")
                                .callbackData("MY_BOOKINGS:")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🧑 My Profile")
                                .callbackData("MY_PROFILE:")
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📞 Help")
                                .callbackData("HELP:")
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarCategoryKeyboard(List<CategoryAvailabilityDto> availability) {
        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (CategoryAvailabilityDto dto : availability) {
            String emoji = getCategoryEmoji(dto.category());
            int available = dto.count().intValue();

            InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                    .text(String.format("%s %s (%d cars available)", emoji, dto.category().name(), available))
                    .callbackData("BROWSE_CARS:" + dto.category().name())
                    .build();

            rows.add(List.of(button));
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData("BACK")
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
                .callbackData("BACK")
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
                .callbackData("BACK")
                .build();
        rows.add(List.of(button, backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }
}

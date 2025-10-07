package org.example.carrentalbot.util;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                    .callbackData("BROWSE_CARS_CHOICE:" + dto.category().name())
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

    public InlineKeyboardMarkupDto buildCarChoiceKeyboard() {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto allCarsButton = InlineKeyboardButtonDto.builder()
                .text("All Cars")
                .callbackData("BROWSE_ALL_CARS")
                .build();
        rows.add(List.of(allCarsButton));

        InlineKeyboardButtonDto carsForMyDatesButton = InlineKeyboardButtonDto.builder()
                .text("Cars For My Dates")
                .callbackData("BROWSE_CARS_FOR_DATES")
                .build();
        rows.add(List.of(carsForMyDatesButton));

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData("GO_BACK")
                .build();
        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildCarsKeyboard(List<Car> cars) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (Car car : cars) {
            InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                    .text(String.format("%s  %s (%s)", "🔸", car.getBrand(), car.getModel()))
                    .callbackData("DISPLAY_CAR_DETAILS:" + car.getId())
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

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(String carBrowsingMode) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        switch (carBrowsingMode) {
            case "BROWSE_ALL_CARS" -> {
                InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                        .text("CHECK AVAILABILITY")
                        .callbackData("CHECK AVAILABILITY")
                        .build();
                rows.add(List.of(button));
            }

            case "BROWSE_CARS_FOR_DATES" -> {
                InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("BOOK")
                .callbackData("BOOK_CAR")
                .build();
                rows.add(List.of(button));
            }
            default -> log.warn("Unknown car browsing mode: {}", carBrowsingMode);
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

    public InlineKeyboardMarkupDto buildConfirmRentalDatesKeyboard() {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto confirmButton = InlineKeyboardButtonDto.builder()
                .text("✅ Confirm")
                .callbackData("CONFIRM_RENTAL_DATES")
                .build();

        rows.add(List.of(confirmButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }
}

package org.example.carrentalbot.util;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CarProjectionDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardButtonDto;
import org.example.carrentalbot.handler.callback.*;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
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
                                .callbackData(BrowseCategoriesHandler.KEY)
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
                    .callbackData(ChooseCarBrowsingModeHandler.KEY + ":" + dto.category().name())
                    .build();

            rows.add(List.of(button));
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
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

    public InlineKeyboardMarkupDto buildCarBrowsingModeKeyboard() {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto allCarsButton = InlineKeyboardButtonDto.builder()
                .text("All Cars")
                .callbackData(BrowseAllCarsHandler.KEY + ":" + CarBrowsingMode.ALL_CARS.name())
                .build();
        rows.add(List.of(allCarsButton));

        InlineKeyboardButtonDto carsForMyDatesButton = InlineKeyboardButtonDto.builder()
                .text("Cars For My Dates")
                .callbackData(AskForRentalDatesHandler.KEY + ":" + CarBrowsingMode.CARS_FOR_DATES.name())
                .build();
        rows.add(List.of(carsForMyDatesButton));

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
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
                    .callbackData(DisplayCarDetailsHandler.KEY + ":" + car.getId())
                    .build();

            rows.add(List.of(button));
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
                .build();

        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(CarBrowsingMode carBrowsingMode) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        switch (carBrowsingMode) {
            case ALL_CARS -> {
                InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                        .text("🕒 CHECK AVAILABILITY")
                        .callbackData(AskForRentalDatesHandler.KEY)//add  + ":" + CarBrowsingMode.ALL_CARS.name(), if not working properly
                        .build();
                rows.add(List.of(button));
            }

            case CARS_FOR_DATES -> {
                InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("📝 START BOOKING")
                .callbackData(AskForPhoneHandler.KEY)
                .build();
                rows.add(List.of(button));
            }
            default -> log.warn("Unknown car browsing mode: {}", carBrowsingMode);
        }

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
                .build();
        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildConfirmKeyboard(String callbackKey) {
        InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("✅ CONFIRM")
                .callbackData(callbackKey)
                .build();

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(List.of(button)))
                .build();
    }

    public InlineKeyboardMarkupDto buildConfirmRentalDatesKeyboard(CarBrowsingMode carBrowsingMode) {

        String callbackKey = switch (carBrowsingMode) {
            case CARS_FOR_DATES -> BrowseCarsForDatesHandler.KEY;
            case ALL_CARS -> CheckCarAvailabilityHandler.KEY;
        };

        return buildConfirmKeyboard(callbackKey);
    }

    public InlineKeyboardMarkupDto buildCarAvailableKeyboard() {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("📝 START BOOKING")
                .callbackData(AskForPhoneHandler.KEY)
                .build();
        rows.add(List.of(button));

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
                .build();
        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildCarUnavailableKeyboard() {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        InlineKeyboardButtonDto button = InlineKeyboardButtonDto.builder()
                .text("🗓️ CHANGE DATES")
                .callbackData(AskForRentalDatesHandler.KEY)
                .build();
        rows.add(List.of(button));

        InlineKeyboardButtonDto backButton = InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK TO CARS")
                .callbackData(BrowseAllCarsHandler.KEY)
                .build();
        rows.add(List.of(backButton));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }
}

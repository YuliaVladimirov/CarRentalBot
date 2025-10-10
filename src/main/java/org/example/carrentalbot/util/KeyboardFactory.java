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

            rows.add(List.of(InlineKeyboardButtonDto.builder()
                    .text(String.format("%s %s - from €%s/day", emoji, dto.category().getValue(), minimalDailyRate))
                    .callbackData(ChooseCarBrowsingModeHandler.KEY + ":" + dto.category().name())
                    .build()));
        }

        rows.add(List.of(InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
                .build()));

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

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("All Cars")
                                .callbackData(BrowseAllCarsHandler.KEY + ":" + CarBrowsingMode.ALL_CARS.name())
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("Cars For My Dates")
                                .callbackData(AskForRentalDatesHandler.KEY + ":" + CarBrowsingMode.CARS_FOR_DATES.name())
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ BACK")
                                .callbackData(GoBackHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarsKeyboard(List<Car> cars) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (Car car : cars) {

            rows.add(List.of(InlineKeyboardButtonDto.builder()
                    .text(String.format("%s  %s (%s)", "🔸", car.getBrand(), car.getModel()))
                    .callbackData(DisplayCarDetailsHandler.KEY + ":" + car.getId())
                    .build()));
        }

        rows.add(List.of(InlineKeyboardButtonDto.builder()
                .text("⬅️ BACK")
                .callbackData(GoBackHandler.KEY)
                .build()));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(CarBrowsingMode carBrowsingMode) {

        String text = null;
        String callbackKey = null;

        switch (carBrowsingMode) {
            case ALL_CARS -> {
                text = "🕒 CHECK AVAILABILITY";
                callbackKey = AskForRentalDatesHandler.KEY;
            }
            case CARS_FOR_DATES -> {
                text = "📝 START BOOKING";
                callbackKey = AskForPhoneHandler.KEY;
            }
            default -> log.warn("Unknown car browsing mode: {}", carBrowsingMode);
        }

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text(text)
                                .callbackData(callbackKey)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ BACK")
                                .callbackData(GoBackHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildConfirmKeyboard(String callbackKey) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(List.of(InlineKeyboardButtonDto.builder()
                        .text("✅ CONFIRM")
                        .callbackData(callbackKey)
                        .build())))
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

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📝 START BOOKING")
                                .callbackData(AskForPhoneHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ BACK")
                                .callbackData(GoBackHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarUnavailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🗓️ CHANGE DATES")
                                .callbackData(AskForRentalDatesHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ BACK TO CARS")
                                .callbackData(BrowseAllCarsHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildBookingDetailsKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ CONFIRM")
                                .callbackData(ConfirmBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✏️ EDIT")
                                .callbackData(EditBookingDetailsHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildEditBookingKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📅 Edit Dates")
                                .callbackData(AskForRentalDatesHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📞 Edit Phone")
                                .callbackData(AskForPhoneHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📧 Edit Email")
                                .callbackData(AskForEmailHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ BACK")
                                .callbackData(GoBackHandler.KEY)
                                .build())
                ))
                .build();
    }
}

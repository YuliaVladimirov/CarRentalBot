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
import java.util.UUID;

@Component
@Slf4j
public class KeyboardFactory {

    public InlineKeyboardMarkupDto buildMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🚗 Browse")
                                .callbackData(BrowseCategoriesHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📒 My Bookings")
                                .callbackData(DisplayMyBookingsHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📞 Help")
                                .callbackData(HelpMenuHandler.KEY)
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
                .text("⬅️ To Main Menu")
                .callbackData(MainMenuHandler.KEY)
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
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
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
                .text("⬅️ To Main Menu")
                .callbackData(MainMenuHandler.KEY)
                .build()));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildOkKeyboard(String callbackKey) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of
                        (List.of(InlineKeyboardButtonDto.builder()
                        .text("✅ OK")
                        .callbackData(callbackKey)
                        .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildToMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(String callbackKey, String text) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text(text)
                                .callbackData(callbackKey)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarAvailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🚀 Start Booking")
                                .callbackData(StartBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarUnavailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🗓️ Change Dates")
                                .callbackData(AskForRentalDatesHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildStartBookingKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ Ok")
                                .callbackData(AskForPhoneHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildBookingDetailsKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ Confirm Booking")
                                .callbackData(ConfirmBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✏️ Edit Contact Info")
                                .callbackData(EditBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("❌ Cancel Booking")
                                .callbackData(CancelBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCancelBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ Yes, Cancel")
                                .callbackData(ConfirmCancelBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ No, Go Back")
                                .callbackData(DisplayBookingDetailsHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildMyBookingsKeyboard(UUID bookingId) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("ℹ️ Details")
                                .callbackData(DisplayMyBookingDetailsHandler.KEY + ":" + bookingId)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildMyBookingDetailsKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✏️ Edit Contact Info")
                                .callbackData(EditMyBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("❌ Cancel Booking")
                                .callbackData(CancelMyBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildEditBookingKeyboard(String callbackKey) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(

                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📞 Edit Phone")
                                .callbackData(AskForPhoneHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("📧 Edit Email")
                                .callbackData(AskForEmailHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ Continue")
                                .callbackData(callbackKey)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCancelMyBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("✅ Yes, Cancel")
                                .callbackData(ConfirmCancelMyBookingHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ Back To Booking Details")
                                .callbackData(DisplayMyBookingDetailsHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("⬅️ To Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildHelpMenuKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🏠 Main Menu")
                                .callbackData(MainMenuHandler.KEY)
                                .build()),
                        List.of(InlineKeyboardButtonDto.builder()
                                .text("🚗 Browse Cars")
                                .callbackData(BrowseCategoriesHandler.KEY)
                                .build()),
                        List.of (InlineKeyboardButtonDto.builder()
                                .text("ℹ️ Help")
                                .callbackData(HelpMenuHandler.KEY)
                                .build()),
                        List.of (InlineKeyboardButtonDto.builder()
                                .text("📞 Contact Support")
                                .url("https://example.com/support")
                                .build())
                ))
                .build();
    }
}

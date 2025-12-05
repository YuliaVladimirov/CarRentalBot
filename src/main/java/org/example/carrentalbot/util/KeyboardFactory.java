package org.example.carrentalbot.util;

import org.example.carrentalbot.record.CarProjection;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.InlineKeyboardButtonDto;
import org.example.carrentalbot.handler.callback.*;
import org.example.carrentalbot.model.Car;
import org.example.carrentalbot.model.enums.CalendarAction;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class KeyboardFactory {

    private InlineKeyboardButtonDto button(String text, String data) {
        return InlineKeyboardButtonDto.builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    public InlineKeyboardMarkupDto buildMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🚗 Browse", BrowseCategoriesHandler.KEY)),
                        List.of(button("📒 My Bookings", DisplayMyBookingsHandler.KEY)),
                        List.of(button("📞 Help", HelpMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarCategoryKeyboard(List<CarProjection> availability) {
        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (CarProjection dto : availability) {
            String emoji = getCategoryEmoji(dto.category());
            BigDecimal minimalDailyRate = dto.minimalDailyRate().setScale(0, RoundingMode.HALF_UP);

            rows.add(List.of(button(
                    String.format("%s %s - from €%s/day", emoji, dto.category().getValue(), minimalDailyRate),
                    ChooseCarBrowsingModeHandler.KEY + ":" + dto.category().name())));
        }

        rows.add(List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY)));

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
                        List.of(button("All Cars", BrowseAllCarsHandler.KEY + ":" + CarBrowsingMode.ALL_CARS.name())),
                        List.of(button("Cars For My Dates", AskForStartDateHandler.KEY + ":" + CarBrowsingMode.CARS_FOR_DATES.name())),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarsKeyboard(List<Car> cars) {

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (Car car : cars) {

            rows.add(List.of(button(
                    String.format("%s  %s (%s)", "🔸", car.getBrand(), car.getModel()),
                    DisplayCarDetailsHandler.KEY + ":" + car.getId())));
        }

        rows.add(List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY)));

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(rows)
                .build();
    }

    public InlineKeyboardMarkupDto buildConfirmDatesKeyboard(String callbackKey) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Confirm", callbackKey)),
                        List.of(button("🔄 Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu",MainMenuHandler.KEY ))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildInvalidDatesKeyboard(){
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🔄 Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildOkKeyboard(String callbackKey) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of
                        (List.of(button("✅ OK", callbackKey))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildToMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(String callbackKey, String text) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button(text, callbackKey)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarAvailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🚀 Start Booking", StartBookingHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCarUnavailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🗓️ Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildStartBookingKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Ok", AskForPhoneHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildBookingDetailsKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Confirm Booking", ConfirmBookingHandler.KEY)),
                        List.of(button("✏️ Edit Contact Info", EditBookingHandler.KEY)),
                        List.of(button("❌ Cancel Booking", CancelBookingHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCancelBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Yes, Cancel", ConfirmCancelBookingHandler.KEY)),
                        List.of(button("⬅️ No, Go Back", DisplayBookingDetailsHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildMyBookingsKeyboard(UUID bookingId) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("ℹ️ Details", DisplayMyBookingDetailsHandler.KEY + ":" + bookingId)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildMyBookingDetailsKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✏️ Edit Contact Info", EditMyBookingHandler.KEY)),
                        List.of(button("❌ Cancel Booking", CancelMyBookingHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildEditBookingKeyboard(String callbackKey) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("📞 Edit Phone", AskForPhoneHandler.KEY)),
                        List.of(button("📧 Edit Email", AskForEmailHandler.KEY)),
                        List.of(button("✅ Continue", callbackKey)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCancelMyBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Yes, Cancel", ConfirmCancelMyBookingHandler.KEY)),
                        List.of(button("⬅️ To Booking Details", DisplayMyBookingDetailsHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildHelpMenuKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🏠 Main Menu", MainMenuHandler.KEY)),
                        List.of(button("🚗 Browse Cars", BrowseCategoriesHandler.KEY)),
                        List.of (button("ℹ️ Help", HelpMenuHandler.KEY)),
                        List.of (InlineKeyboardButtonDto.builder()
                                .text("📞 Contact Support")
                                .url("https://example.com/support")
                                .build())
                ))
                .build();
    }

    public InlineKeyboardMarkupDto buildCalendar(int year, int month, String prefix) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate first = yearMonth.atDay(1);

        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        rows.add(List.of(
                button("«", prefix + CalendarAction.PREV + ":" + year + ":" + month),
                button(yearMonth.getMonth().name() + " " + year, prefix + CalendarAction.IGNORE),
                button("»", prefix +CalendarAction.NEXT + ":" + year + ":" + month)
        ));

        rows.add(List.of(
                button("Mo", prefix + CalendarAction.IGNORE),
                button("Tu", prefix + CalendarAction.IGNORE),
                button("We", prefix + CalendarAction.IGNORE),
                button("Th", prefix + CalendarAction.IGNORE),
                button("Fr", prefix + CalendarAction.IGNORE),
                button("Sa", prefix + CalendarAction.IGNORE),
                button("Su", prefix + CalendarAction.IGNORE)
        ));

        LocalDate currentDate = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        do {
            List<InlineKeyboardButtonDto> week = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                if (currentDate.getMonthValue() == month) {
                    week.add(button(
                            String.valueOf(currentDate.getDayOfMonth()),
                            prefix + CalendarAction.PICK + ":" + currentDate
                    ));
                } else {
                    week.add(button(" ", prefix + CalendarAction.IGNORE));
                }
                currentDate = currentDate.plusDays(1);
            }

            rows.add(week);

        } while (currentDate.getMonthValue() == month || currentDate.getDayOfWeek() != DayOfWeek.MONDAY);

        return new InlineKeyboardMarkupDto(rows);
    }
}

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

/**
 * Factory component responsible for building Telegram inline keyboards
 * used across bot handlers.
 * <p>This class centralizes creation of all {@link InlineKeyboardMarkupDto}
 * instances to ensure consistency of button layout, callback data structure,
 * and UI formatting across the application.</p>
 */
@Component
public class KeyboardFactory {

    /**
     * Creates a single inline keyboard button with callback data.
     *
     * @param text visible text displayed on the button
     * @param data callback data sent to Telegram bot on click
     * @return constructed {@link InlineKeyboardButtonDto}
     */
    private InlineKeyboardButtonDto button(String text, String data) {
        return InlineKeyboardButtonDto.builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    /**
     * Builds the main menu keyboard shown to the user after start.
     *
     * @return main menu keyboard with navigation options
     */
    public InlineKeyboardMarkupDto buildMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🚗 Browse", BrowseCategoriesHandler.KEY)),
                        List.of(button("📒 My Bookings", DisplayMyBookingsHandler.KEY)),
                        List.of(button("📞 Help", HelpMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard with available car categories and minimal pricing.
     *
     * @param availability list of car category projections with pricing info
     * @return category selection keyboard
     */
    public InlineKeyboardMarkupDto buildCarCategoryKeyboard(List<CarProjection> availability) {
        List<List<InlineKeyboardButtonDto>> rows = new ArrayList<>();

        for (CarProjection projection : availability) {
            String emoji = getCategoryEmoji(projection.category());
            BigDecimal minimalDailyRate = projection.minimalDailyRate().setScale(0, RoundingMode.HALF_UP);

            rows.add(List.of(button(
                    String.format("%s %s - from €%s/day", emoji, projection.category().getValue(), minimalDailyRate),
                    ChooseCarBrowsingModeHandler.KEY + ":" + projection.category().name())));
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

    /**
     * Builds keyboard for selecting car browsing mode.
     *
     * @return browsing mode selection keyboard
     */
    public InlineKeyboardMarkupDto buildCarBrowsingModeKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("All Cars", BrowseAllCarsHandler.KEY + ":" + CarBrowsingMode.ALL_CARS.name())),
                        List.of(button("Cars For My Dates", AskForStartDateHandler.KEY + ":" + CarBrowsingMode.CARS_FOR_DATES.name())),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard listing available cars for selection.
     *
     * @param cars list of available cars
     * @return car selection keyboard
     */
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

    /**
     * Builds confirmation keyboard for selected booking dates.
     *
     * @param callbackKey callback to continue booking flow
     * @return confirmation keyboard
     */
    public InlineKeyboardMarkupDto buildConfirmDatesKeyboard(String callbackKey) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Confirm", callbackKey)),
                        List.of(button("🔄 Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu",MainMenuHandler.KEY ))
                ))
                .build();
    }

    /**
     * Builds keyboard shown when selected dates are invalid.
     *
     * @return retry or navigation keyboard
     */
    public InlineKeyboardMarkupDto buildInvalidDatesKeyboard(){
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🔄 Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds simple confirmation keyboard with a single OK action.
     *
     * @param callbackKey callback action to execute on confirmation
     * @return OK keyboard
     */
    public InlineKeyboardMarkupDto buildOkKeyboard(String callbackKey) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of
                        (List.of(button("✅ OK", callbackKey))
                ))
                .build();
    }

    /**
     * Builds keyboard with navigation back to main menu.
     *
     * @return main menu navigation keyboard
     */
    public InlineKeyboardMarkupDto buildToMainMenuKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard for car details view with custom action button.
     *
     * @param callbackKey action callback
     * @param text button label text
     * @return car details keyboard
     */
    public InlineKeyboardMarkupDto buildCarDetailsKeyboard(String callbackKey, String text) {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button(text, callbackKey)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard shown when a car is available for booking.
     *
     * @return availability action keyboard
     */
    public InlineKeyboardMarkupDto buildCarAvailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🚀 Start Booking", StartBookingHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard shown when a car is not available for selected dates.
     *
     * @return unavailable state keyboard
     */
    public InlineKeyboardMarkupDto buildCarUnavailableKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("🗓️ Change Dates", AskForStartDateHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard to start booking flow.
     *
     * @return start booking confirmation keyboard
     */
    public InlineKeyboardMarkupDto buildStartBookingKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Ok", AskForPhoneHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard for booking management actions.
     *
     * @return booking details keyboard
     */
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

    /**
     * Builds confirmation keyboard for booking cancellation.
     *
     * @return cancellation confirmation keyboard
     */
    public InlineKeyboardMarkupDto buildCancelBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Yes, Cancel", ConfirmCancelBookingHandler.KEY)),
                        List.of(button("⬅️ No, Go Back", DisplayBookingDetailsHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard for listing user's bookings.
     *
     * @param bookingId booking identifier
     * @return my bookings keyboard
     */
    public InlineKeyboardMarkupDto buildMyBookingsKeyboard(UUID bookingId) {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("ℹ️ Details", DisplayMyBookingDetailsHandler.KEY + ":" + bookingId)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard for viewing a specific booking details.
     *
     * @return booking details actions keyboard
     */
    public InlineKeyboardMarkupDto buildMyBookingDetailsKeyboard() {
        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✏️ Edit Contact Info", EditMyBookingHandler.KEY)),
                        List.of(button("❌ Cancel Booking", CancelMyBookingHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds keyboard for editing booking information.
     *
     * @param callbackKey continuation callback action
     * @return edit booking keyboard
     */
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

    /**
     * Builds keyboard for canceling a user's booking.
     *
     * @return cancel booking confirmation keyboard
     */
    public InlineKeyboardMarkupDto buildCancelMyBookingKeyboard() {

        return InlineKeyboardMarkupDto.builder()
                .inlineKeyboard(List.of(
                        List.of(button("✅ Yes, Cancel", ConfirmCancelMyBookingHandler.KEY)),
                        List.of(button("⬅️ To Booking Details", DisplayMyBookingDetailsHandler.KEY)),
                        List.of(button("⬅️ To Main Menu", MainMenuHandler.KEY))
                ))
                .build();
    }

    /**
     * Builds help menu keyboard with navigation and external support link.
     *
     * @return help menu keyboard
     */
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

    /**
     * Builds inline calendar keyboard for date selection.
     *
     * <p>Supports month navigation and day selection callbacks.</p>
     *
     * @param year calendar year
     * @param month calendar month (1-12)
     * @param prefix callback prefix for handling actions
     * @return calendar keyboard
     */
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

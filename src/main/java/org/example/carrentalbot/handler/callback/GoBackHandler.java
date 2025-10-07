package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class GoBackHandler implements CallbackHandler {

    private final NavigationService navigationService;
    private final GoToMainMenuHandler goToMainMenuHandler;
    private final BrowseCategoriesHandler browseCategoriesHandler;
    private final ChooseCarBrowsingModeHandler chooseCarBrowsingModeHandler;
    private final BrowseAllCarsHandler browseAllCarsHandler;
    private final DisplayCarDetailsHandler displayCarDetailsHandler;
    private final BrowseCarsForDatesHandler browseCarsForDatesHandler;
    private final ConfirmRentalDaysHandler confirmRentalDaysHandler;



    public GoBackHandler(NavigationService navigationService,
                         GoToMainMenuHandler goToMainMenuHandler,
                         BrowseCategoriesHandler browseCategoriesHandler,
                         BrowseAllCarsHandler browseAllCarsHandler,
                         BrowseCarsForDatesHandler browseCarsForDatesHandler,
                         DisplayCarDetailsHandler displayCarDetailsHandler,
                         ChooseCarBrowsingModeHandler chooseCarBrowsingModeHandler, ConfirmRentalDaysHandler confirmRentalDaysHandler) {
        this.navigationService = navigationService;
        this.goToMainMenuHandler = goToMainMenuHandler;
        this.browseCategoriesHandler = browseCategoriesHandler;
        this.browseAllCarsHandler = browseAllCarsHandler;
        this.browseCarsForDatesHandler = browseCarsForDatesHandler;
        this.displayCarDetailsHandler = displayCarDetailsHandler;
        this.chooseCarBrowsingModeHandler = chooseCarBrowsingModeHandler;
        this.confirmRentalDaysHandler = confirmRentalDaysHandler;
    }

    @Override
    public String getKey() {
        return "GO_BACK";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String previousState = navigationService.pop(chatId);

        if (previousState == null) {
            goToMainMenuHandler.handle(chatId, callbackQuery);
            return;
        }

        switch (previousState) {
            case "CONFIRM_RENTAL_DATES" -> confirmRentalDaysHandler.handle(chatId, callbackQuery);
            case "BROWSE_CARS_FOR_DATES" -> browseCarsForDatesHandler.handle(chatId, callbackQuery);
            case "DISPLAY_CAR_DETAILS" -> displayCarDetailsHandler.handle(chatId, callbackQuery);
            case "BROWSE_ALL_CARS" -> browseAllCarsHandler.handle(chatId, callbackQuery);
            case "CHOOSE_CAR_BROWSING_MODE" -> chooseCarBrowsingModeHandler.handle(chatId, callbackQuery);
            case "BROWSE_CATEGORIES" -> browseCategoriesHandler.handle(chatId, callbackQuery);

            default -> goToMainMenuHandler.handle(chatId, callbackQuery);
        }
    }
}

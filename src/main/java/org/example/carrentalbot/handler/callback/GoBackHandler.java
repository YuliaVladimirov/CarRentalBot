package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class GoBackHandler implements CallbackHandler {

    public static final String KEY = "GO_BACK";

    private final NavigationService navigationService;
    private final GoToMainMenuHandler goToMainMenuHandler;
    private final BrowseCategoriesHandler browseCategoriesHandler;
    private final ChooseCarBrowsingModeHandler chooseCarBrowsingModeHandler;
    private final BrowseAllCarsHandler browseAllCarsHandler;
    private final DisplayCarDetailsHandler displayCarDetailsHandler;
    private final AskForRentalDatesHandler askForRentalDatesHandler;
    private final BrowseCarsForDatesHandler browseCarsForDatesHandler;
    private final CheckCarAvailabilityHandler checkCarAvailabilityHandler;

    public GoBackHandler(NavigationService navigationService,
                         GoToMainMenuHandler goToMainMenuHandler,
                         BrowseCategoriesHandler browseCategoriesHandler,
                         BrowseAllCarsHandler browseAllCarsHandler,
                         AskForRentalDatesHandler askForRentalDatesHandler,
                         DisplayCarDetailsHandler displayCarDetailsHandler,
                         ChooseCarBrowsingModeHandler chooseCarBrowsingModeHandler,
                         BrowseCarsForDatesHandler browseCarsForDatesHandler, CheckCarAvailabilityHandler checkCarAvailabilityHandler) {
        this.navigationService = navigationService;
        this.goToMainMenuHandler = goToMainMenuHandler;
        this.browseCategoriesHandler = browseCategoriesHandler;
        this.browseAllCarsHandler = browseAllCarsHandler;
        this.askForRentalDatesHandler = askForRentalDatesHandler;
        this.displayCarDetailsHandler = displayCarDetailsHandler;
        this.chooseCarBrowsingModeHandler = chooseCarBrowsingModeHandler;
        this.browseCarsForDatesHandler = browseCarsForDatesHandler;
        this.checkCarAvailabilityHandler = checkCarAvailabilityHandler;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String previousState = navigationService.pop(chatId);

        if (previousState == null) {
            goToMainMenuHandler.handle(chatId, callbackQuery);
            return;
        }

        switch (previousState) {
            case CheckCarAvailabilityHandler.KEY -> checkCarAvailabilityHandler.handle(chatId, callbackQuery);
            case BrowseCarsForDatesHandler.KEY -> browseCarsForDatesHandler.handle(chatId, callbackQuery);
            case AskForRentalDatesHandler.KEY -> askForRentalDatesHandler.handle(chatId, callbackQuery);
            case DisplayCarDetailsHandler.KEY -> displayCarDetailsHandler.handle(chatId, callbackQuery);
            case BrowseAllCarsHandler.KEY -> browseAllCarsHandler.handle(chatId, callbackQuery);
            case ChooseCarBrowsingModeHandler.KEY -> chooseCarBrowsingModeHandler.handle(chatId, callbackQuery);
            case BrowseCategoriesHandler.KEY -> browseCategoriesHandler.handle(chatId, callbackQuery);

            default -> goToMainMenuHandler.handle(chatId, callbackQuery);
        }
    }
}

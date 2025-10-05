package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class GoBackHandler implements CallbackHandler {

    private final NavigationService navigationService;
    private final GoToMainMenuHandler goToMainMenuHandler;
    private final BrowseCategoriesHandler browseCategoriesHandler;
    private final BrowseAllCarsHandler browseAllCarsHandler;
    private final CarDetailsHandler carDetailsHandler;

    public GoBackHandler(NavigationService navigationService, GoToMainMenuHandler goToMainMenuHandler, BrowseCategoriesHandler browseCategoriesHandler, BrowseAllCarsHandler browseAllCarsHandler, CarDetailsHandler carDetailsHandler) {
        this.navigationService = navigationService;
        this.goToMainMenuHandler = goToMainMenuHandler;
        this.browseCategoriesHandler = browseCategoriesHandler;
        this.browseAllCarsHandler = browseAllCarsHandler;
        this.carDetailsHandler = carDetailsHandler;
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
            case "CAR_DETAILS" -> carDetailsHandler.handle(chatId, callbackQuery);
            case "BROWSE_CARS" -> browseAllCarsHandler.handle(chatId, callbackQuery);
            case "BROWSE_CATEGORIES" -> browseCategoriesHandler.handle(chatId, callbackQuery);

            default -> goToMainMenuHandler.handle(chatId, callbackQuery);
        }
    }
}

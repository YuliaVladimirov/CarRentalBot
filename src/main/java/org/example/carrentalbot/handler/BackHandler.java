package org.example.carrentalbot.handler;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.service.NavigationService;
import org.springframework.stereotype.Component;

@Component
public class BackHandler implements CallbackHandler {


    private final NavigationService navigationService;
    private final ToMainMenuHandler toMainMenuHandler;
    private final BrowseCategoriesHandler browseCategoriesHandler;
    private final MyProfileHandler myProfileHandler;
    private final HelpHandler helpHandler;

    public BackHandler(NavigationService navigationService, ToMainMenuHandler toMainMenuHandler, BrowseCategoriesHandler browseCategoriesHandler, MyProfileHandler myProfileHandler, HelpHandler helpHandler) {
        this.navigationService = navigationService;
        this.toMainMenuHandler = toMainMenuHandler;
        this.browseCategoriesHandler = browseCategoriesHandler;
        this.myProfileHandler = myProfileHandler;
        this.helpHandler = helpHandler;
    }

    @Override
    public String getKey() {
        return "BACK";
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {

        String previousState = navigationService.pop(chatId);

        if (previousState == null) {
            toMainMenuHandler.handle(chatId, callbackQuery);
            return;
        }

        switch (previousState) {
            case "BROWSE_CATEGORIES" -> browseCategoriesHandler.handle(chatId, callbackQuery);
            case "MY_PROFILE" -> myProfileHandler.handle(chatId, callbackQuery);
            case "HELP" -> helpHandler.handle(chatId, callbackQuery);

            default -> toMainMenuHandler.handle(chatId, callbackQuery);
        }
    }
}

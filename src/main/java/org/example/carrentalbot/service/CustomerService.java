package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;

public interface CustomerService {
    Customer registerIfNotExists(Long chatId, FromDto telegramUser);
}

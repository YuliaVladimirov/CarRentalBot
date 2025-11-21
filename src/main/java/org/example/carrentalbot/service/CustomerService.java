package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.record.CustomerRegistration;

public interface CustomerService {
    CustomerRegistration registerIfNotExists(Long chatId, FromDto telegramUser);
}

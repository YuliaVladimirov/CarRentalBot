package org.example.carrentalbot.service;

import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(
            CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer registerIfNotExists(Long chatId, FromDto telegramUser) {

        return customerRepository.findByTelegramUserId(telegramUser.getId())
                .orElseGet(() -> customerRepository.saveAndFlush(
                        Customer.builder()
                                .telegramUserId(telegramUser.getId())
                                .chatId(chatId)
                                .userName(telegramUser.getUserName())
                                .firstName(telegramUser.getFirstName())
                                .lastName(telegramUser.getLastName())
                                .build()
                ));
    }
}

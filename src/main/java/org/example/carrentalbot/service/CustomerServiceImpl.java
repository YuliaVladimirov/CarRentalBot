package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
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

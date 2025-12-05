package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.record.CustomerRegistration;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerRegistration registerIfNotExists(Long chatId, FromDto telegramUser) {

        log.debug("Attempting to find customer");
        return customerRepository.findByTelegramUserId(telegramUser.getId())
                .map(customer -> {
                    log.debug("Found existing customer with id: {}", customer.getId());
                    return new CustomerRegistration(customer, false);
                })
                .orElseGet(() -> {
                    log.debug("Customer not found. Creating new customer");
                    Customer newCustomer = Customer.builder()
                            .telegramUserId(telegramUser.getId())
                            .chatId(chatId)
                            .userName(telegramUser.getUserName())
                            .firstName(telegramUser.getFirstName())
                            .lastName(telegramUser.getLastName())
                            .build();
                    log.debug("Saving new customer");
                    Customer savedCustomer = customerRepository.saveAndFlush(newCustomer);
                    return new CustomerRegistration(savedCustomer, true);

                });
    }
}

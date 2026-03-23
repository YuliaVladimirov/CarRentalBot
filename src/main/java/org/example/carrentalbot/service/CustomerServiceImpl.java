package org.example.carrentalbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.model.Customer;
import org.example.carrentalbot.record.CustomerRegistration;
import org.example.carrentalbot.repository.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link CustomerService}.
 * <p>Handles the lifecycle of a {@link Customer} entity, ensuring that every
 * Telegram user interacting with the bot is correctly mapped to a persistent
 * database record.</p>
 * <p>Uses {@link CustomerRepository} to perform idempotent registration
 * and leverages {@link CustomerRegistration} to signal state changes back to
 * the calling controllers or handlers.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Executes the registration logic using a functional optional-based flow.
     * <ol>
     * <li>Attempts to retrieve the customer by {@code telegramUserId}.</li>
     * <li>If present: Wraps the existing customer in a registration object with {@code isNew = false}.</li>
     * <li>If absent: Builds, flushes, and returns a new {@link Customer} with {@code isNew = true}.</li>
     * </ol>
     * @param chatId       The chat ID provided by the Telegram API.
     * @param telegramUser The user metadata provided by the Telegram API.
     * @return The resulting {@link CustomerRegistration} wrapper.
     */
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

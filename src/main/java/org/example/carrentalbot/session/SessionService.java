package org.example.carrentalbot.session;

import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {
    void put(Long chatId, String field, Object value);
    Optional<String> getString(Long chatId, String field);
    Optional<UUID> getUUID(Long chatId, String field);
    Optional<LocalDate> getLocalDate(Long chatId, String field);
    Optional<Integer> getInteger(Long chatId, String field);
    Optional<BigDecimal> getBigDecimal(Long chatId, String field);
    Optional<CarCategory> getCarCategory(Long chatId, String field);
    Optional<FlowContext> getFlowContext(Long chatId, String field);
    Optional<CarBrowsingMode> getCarBrowsingMode(Long chatId, String field);
    void deleteAll(Long chatId);
}

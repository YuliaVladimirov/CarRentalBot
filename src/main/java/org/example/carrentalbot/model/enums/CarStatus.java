package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum CarStatus {

    IN_SERVICE,
    AWAITING_PREPARATION,
    UNDER_MAINTENANCE,
    UNDER_REPAIR,
    DAMAGED,
    RETIRED
}

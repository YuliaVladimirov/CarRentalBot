package org.example.carrentalbot.model.enums;

import lombok.Getter;

@Getter
public enum CarStatus {

    IN_SERVICE ("In Service"),
    AWAITING_PREPARATION ("Awaiting Preparation"),
    UNDER_MAINTENANCE ("Under Maintenance"),
    UNDER_REPAIR ("Under Repair"),
    DAMAGED ("Damaged"),
    RETIRED ("Retired");

    private final String value;
    CarStatus(String value) {this.value = value;}

}

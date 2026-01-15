package com.example.airline.entity.flight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Status {
    DEPARTED("Departed"),
    ARRIVED("Arrived"),
    ON_TIME("On Time"),
    CANCELLED("Cancelled"),
    DELAYED("Delayed"),
    SCHEDULED("Scheduled");

    private final String statusName;

    Status(String statusName) {
        this.statusName = statusName;
    }

    @JsonValue
    public String getStatusName() {
        return statusName;
    }

    @JsonCreator
    public static Status fromString(String statusName) {
        return Arrays.stream(Status.values())
                .filter(status -> status.statusName.equalsIgnoreCase(statusName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}

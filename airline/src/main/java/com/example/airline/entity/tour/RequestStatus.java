package com.example.airline.entity.tour;

public enum RequestStatus {
    NEW("Новая"),
    IN_PROGRESS("В обработке"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


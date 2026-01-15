package com.example.airline.entity.tour;

public enum RequestPriority {
    LOW("Низкий"),
    NORMAL("Обычный"),
    HIGH("Высокий"),
    URGENT("Срочный");

    private final String displayName;

    RequestPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


package com.example.airline.entity.flight;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Optional;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {
    @Override
    public String convertToDatabaseColumn(Status status) {
        return Optional.ofNullable(status)
                .map(Status::getStatusName)
                .orElse(null);
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        return Optional.of(Status.fromString(dbData)).get();
    }
}
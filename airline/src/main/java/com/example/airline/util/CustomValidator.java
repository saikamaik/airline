package com.example.airline.util;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Валидатор проверяющий валидность вписываемых данных
 */
public interface CustomValidator {
    /**
     * Метод для валидации, проверяющий валидность по аннотациям в DTO объекте
     *
     * @param objectDto объект DTO
     * @return
     */
    Set<ConstraintViolation<Object>> validate(Object objectDto);
}

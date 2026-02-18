package com.example.airline.validation;

import com.example.airline.dto.auth.RegisterRequest;
import com.example.airline.dto.tour.TourDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void tourDto_WithValidData_ShouldPassValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Отдых в Сочи");
        dto.setDescription("Комфортабельный отель");
        dto.setPrice(new BigDecimal("45000.00"));
        dto.setDurationDays(7);
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void tourDto_WithBlankName_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("");  // Blank name
        dto.setPrice(new BigDecimal("45000.00"));
        dto.setDurationDays(7);
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void tourDto_WithNullPrice_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Отдых в Сочи");
        dto.setPrice(null);  // Null price
        dto.setDurationDays(7);
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    void tourDto_WithNegativePrice_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Отдых в Сочи");
        dto.setPrice(new BigDecimal("-1000"));  // Negative price
        dto.setDurationDays(7);
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("price") && 
            v.getMessage().contains("positive")
        );
    }

    @Test
    void tourDto_WithZeroDuration_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Отдых в Сочи");
        dto.setPrice(new BigDecimal("45000.00"));
        dto.setDurationDays(0);  // Zero duration
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("durationDays") && 
            v.getMessage().contains("at least 1")
        );
    }

    @Test
    void tourDto_WithBlankDestination_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("Отдых в Сочи");
        dto.setPrice(new BigDecimal("45000.00"));
        dto.setDurationDays(7);
        dto.setDestinationCity("");  // Blank destination

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("destinationCity"));
    }

    @Test
    void tourDto_WithTooLongName_ShouldFailValidation() {
        // Given
        TourDto dto = new TourDto();
        dto.setName("A".repeat(201));  // 201 characters (max is 200)
        dto.setPrice(new BigDecimal("45000.00"));
        dto.setDurationDays(7);
        dto.setDestinationCity("Сочи");

        // When
        Set<ConstraintViolation<TourDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("name") && 
            v.getMessage().contains("200")
        );
    }

    @Test
    void registerRequest_WithValidData_ShouldPassValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("Иван");
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_WithBlankUsername_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");  // Blank username
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("Иван");
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void registerRequest_WithShortUsername_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");  // Too short (min is 3)
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("Иван");
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("username") && 
            v.getMessage().contains("3")
        );
    }

    @Test
    void registerRequest_WithShortPassword_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("12345");  // Too short (min is 6)
        request.setEmail("test@example.com");
        request.setFirstName("Иван");
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") && 
            v.getMessage().contains("6")
        );
    }

    @Test
    void registerRequest_WithInvalidEmail_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("invalid-email");  // Invalid email format
        request.setFirstName("Иван");
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") && 
            v.getMessage().toLowerCase().contains("email")
        );
    }

    @Test
    void registerRequest_WithBlankFirstName_ShouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFirstName("");  // Blank first name
        request.setLastName("Иванов");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }
}

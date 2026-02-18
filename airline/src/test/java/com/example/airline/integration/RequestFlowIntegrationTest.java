package com.example.airline.integration;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.entity.tour.Tour;
import com.example.airline.entity.user.Employee;
import com.example.airline.entity.user.User;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.service.request.ClientRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест полного цикла работы с заявками
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({com.example.airline.service.request.ClientRequestService.class,
        com.example.airline.service.request.RequestHistoryService.class,
        com.example.airline.util.CustomValidator.class,
        com.example.airline.mapper.request.ClientRequestMapper.class,
        com.example.airline.service.notification.EmailService.class})
class RequestFlowIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRequestRepository requestRepository;

    @Autowired
    private ClientRequestService requestService;

    private Tour tour;
    private Client client;
    private Employee employee;

    @BeforeEach
    void setUp() {
        // Создаем тестовый тур
        tour = new Tour();
        tour.setName("Тестовый тур");
        tour.setPrice(new BigDecimal("50000.00"));
        tour.setDurationDays(7);
        tour.setDestinationCity("Сочи");
        tour.setActive(true);
        tour = entityManager.persistAndFlush(tour);

        // Создаем тестового клиента
        client = new Client();
        client.setFirstName("Иван");
        client.setLastName("Иванов");
        client.setEmail("ivan@example.com");
        client.setPhone("+7 (999) 123-45-67");
        client.setVipStatus(false);
        client = entityManager.persistAndFlush(client);

        // Создаем тестового сотрудника
        User user = new User();
        user.setUsername("employee1");
        user.setPassword("password");
        user.setEmail("employee@example.com");
        user = entityManager.persistAndFlush(user);

        employee = new Employee();
        employee.setUser(user);
        employee.setFirstName("Мария");
        employee.setLastName("Петрова");
        employee.setEmail("employee@example.com");
        employee.setActive(true);
        employee = entityManager.persistAndFlush(employee);
    }

    @Test
    void fullRequestFlow_CreateUpdateStatusComplete_ShouldWork() {
        // 1. Создание заявки
        ClientRequestDto createDto = new ClientRequestDto();
        createDto.setTourId(tour.getId());
        createDto.setUserName("Иван Иванов");
        createDto.setUserEmail("ivan@example.com");
        createDto.setUserPhone("+7 (999) 123-45-67");
        createDto.setComment("Хочу на море");

        ClientRequestDto createdRequest = requestService.createRequest(createDto);
        
        assertThat(createdRequest).isNotNull();
        assertThat(createdRequest.getId()).isNotNull();
        assertThat(createdRequest.getStatus()).isEqualTo(RequestStatus.NEW);
        assertThat(createdRequest.getPriority()).isEqualTo(RequestPriority.NORMAL);

        // Проверяем, что заявка сохранена в БД
        ClientRequest savedRequest = requestRepository.findById(createdRequest.getId()).orElse(null);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getStatus()).isEqualTo(RequestStatus.NEW);

        // 2. Изменение статуса на IN_PROGRESS с назначением сотрудника
        ClientRequestDto updatedRequest = requestService.updateStatus(
            createdRequest.getId(), 
            RequestStatus.IN_PROGRESS, 
            employee.getId()
        );

        assertThat(updatedRequest.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        
        // Проверяем в БД
        ClientRequest requestInDb = requestRepository.findById(createdRequest.getId()).orElse(null);
        assertThat(requestInDb).isNotNull();
        assertThat(requestInDb.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(requestInDb.getEmployee()).isNotNull();
        assertThat(requestInDb.getEmployee().getId()).isEqualTo(employee.getId());

        // 3. Изменение приоритета
        ClientRequestDto priorityUpdated = requestService.updatePriority(
            createdRequest.getId(),
            RequestPriority.HIGH,
            employee.getId()
        );

        assertThat(priorityUpdated.getPriority()).isEqualTo(RequestPriority.HIGH);
        
        // Проверяем в БД
        ClientRequest requestWithPriority = requestRepository.findById(createdRequest.getId()).orElse(null);
        assertThat(requestWithPriority).isNotNull();
        assertThat(requestWithPriority.getPriority()).isEqualTo(RequestPriority.HIGH);

        // 4. Завершение заявки
        ClientRequestDto completedRequest = requestService.updateStatus(
            createdRequest.getId(),
            RequestStatus.COMPLETED,
            employee.getId()
        );

        assertThat(completedRequest.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        
        // Проверяем в БД
        ClientRequest finalRequest = requestRepository.findById(createdRequest.getId()).orElse(null);
        assertThat(finalRequest).isNotNull();
        assertThat(finalRequest.getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void createRequest_WithVipClient_ShouldSetHighPriority() {
        // Given
        client.setVipStatus(true);
        entityManager.persistAndFlush(client);

        ClientRequestDto createDto = new ClientRequestDto();
        createDto.setTourId(tour.getId());
        createDto.setUserName("VIP Клиент");
        createDto.setUserEmail("ivan@example.com");
        createDto.setComment("VIP заявка");

        // When
        ClientRequestDto createdRequest = requestService.createRequest(createDto);

        // Then
        assertThat(createdRequest.getPriority()).isEqualTo(RequestPriority.HIGH);
        
        // Проверяем в БД
        ClientRequest savedRequest = requestRepository.findById(createdRequest.getId()).orElse(null);
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getPriority()).isEqualTo(RequestPriority.HIGH);
    }

    @Test
    void findByStatus_ShouldReturnFilteredRequests() {
        // Given - создаем несколько заявок с разными статусами
        ClientRequestDto newRequest = new ClientRequestDto();
        newRequest.setTourId(tour.getId());
        newRequest.setUserName("Клиент 1");
        newRequest.setUserEmail("client1@example.com");
        requestService.createRequest(newRequest);

        ClientRequestDto inProgressRequest = new ClientRequestDto();
        inProgressRequest.setTourId(tour.getId());
        inProgressRequest.setUserName("Клиент 2");
        inProgressRequest.setUserEmail("client2@example.com");
        ClientRequestDto created = requestService.createRequest(inProgressRequest);
        requestService.updateStatus(created.getId(), RequestStatus.IN_PROGRESS, employee.getId());

        // When
        var newRequests = requestService.findByStatus(
            RequestStatus.NEW,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );

        var inProgressRequests = requestService.findByStatus(
            RequestStatus.IN_PROGRESS,
            org.springframework.data.domain.PageRequest.of(0, 10)
        );

        // Then
        assertThat(newRequests.getContent()).isNotEmpty();
        assertThat(newRequests.getContent()).allMatch(r -> r.getStatus() == RequestStatus.NEW);
        
        assertThat(inProgressRequests.getContent()).isNotEmpty();
        assertThat(inProgressRequests.getContent()).allMatch(r -> r.getStatus() == RequestStatus.IN_PROGRESS);
    }
}

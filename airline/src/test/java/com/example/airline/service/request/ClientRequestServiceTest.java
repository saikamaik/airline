package com.example.airline.service.request;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.entity.tour.Tour;
import com.example.airline.entity.user.Employee;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.repository.user.EmployeeRepository;
import com.example.airline.repository.user.UserRepository;
import com.example.airline.service.notification.EmailService;
import com.example.airline.util.CustomValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientRequestServiceTest {

    @Mock
    private ClientRequestRepository requestRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestHistoryService historyService;

    @Mock
    private CustomValidator customValidator;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ClientRequestService clientRequestService;

    private ClientRequestDto requestDto;
    private Tour tour;
    private Client client;
    private ClientRequest request;

    @BeforeEach
    void setUp() {
        tour = new Tour();
        tour.setId(1L);
        tour.setName("Отдых в Сочи");
        tour.setPrice(new BigDecimal("45000.00"));
        tour.setActive(true);

        client = new Client();
        client.setId(1L);
        client.setEmail("test@example.com");
        client.setVipStatus(false);

        requestDto = new ClientRequestDto();
        requestDto.setTourId(1L);
        requestDto.setUserName("Иван Иванов");
        requestDto.setUserEmail("test@example.com");
        requestDto.setUserPhone("+7 (999) 123-45-67");
        requestDto.setComment("Хочу на море");

        request = new ClientRequest();
        request.setId(1L);
        request.setTour(tour);
        request.setUserName("Иван Иванов");
        request.setUserEmail("test@example.com");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(RequestPriority.NORMAL);
    }

    @Test
    void createRequest_WithValidData_ShouldCreateRequest() {
        // Given
        when(customValidator.validate(any(ClientRequestDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(requestRepository.save(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        // When
        ClientRequestDto result = clientRequestService.createRequest(requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(customValidator).validate(requestDto);
        verify(tourRepository).findById(1L);
        verify(requestRepository).save(any(ClientRequest.class));
        verify(historyService).logChange(any(), isNull(), eq("STATUS"), isNull(), anyString(), anyString());
        verify(emailService).sendRequestCreatedNotification(any());
    }

    @Test
    void createRequest_WithNonExistentTour_ShouldThrowException() {
        // Given
        when(customValidator.validate(any(ClientRequestDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        requestDto.setTourId(999L);

        // When/Then
        assertThatThrownBy(() -> clientRequestService.createRequest(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tour not found: 999");
        verify(requestRepository, never()).save(any(ClientRequest.class));
    }

    @Test
    void createRequest_WithVipClient_ShouldSetHighPriority() {
        // Given
        client.setVipStatus(true);
        when(customValidator.validate(any(ClientRequestDto.class))).thenReturn(java.util.Collections.emptySet());
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(clientRepository.findByEmail("test@example.com")).thenReturn(Optional.of(client));
        when(requestRepository.save(any(ClientRequest.class))).thenAnswer(invocation -> {
            ClientRequest req = invocation.getArgument(0);
            req.setId(1L);
            req.setPriority(RequestPriority.HIGH);
            return req;
        });

        // When
        ClientRequestDto result = clientRequestService.createRequest(requestDto);

        // Then
        assertThat(result).isNotNull();
        verify(requestRepository).save(any(ClientRequest.class));
    }

    @Test
    void updateStatus_WithValidData_ShouldUpdateStatus() {
        // Given
        Long requestId = 1L;
        RequestStatus newStatus = RequestStatus.IN_PROGRESS;

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(ClientRequest.class))).thenReturn(request);

        // When
        ClientRequestDto result = clientRequestService.updateStatus(requestId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(request.getStatus()).isEqualTo(newStatus);
        verify(requestRepository).findById(requestId);
        verify(requestRepository).save(request);
        verify(historyService).logChange(any(), isNull(), eq("STATUS"), eq("NEW"), eq("IN_PROGRESS"), anyString());
        verify(emailService).sendStatusChangedNotification(any(), eq(RequestStatus.NEW));
    }

    @Test
    void updateStatus_WithNonExistentId_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(requestRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> clientRequestService.updateStatus(nonExistentId, RequestStatus.IN_PROGRESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request not found: 999");
        verify(requestRepository, never()).save(any(ClientRequest.class));
    }

    @Test
    void updateStatus_WithEmployee_ShouldAssignEmployee() {
        // Given
        Long requestId = 1L;
        Long employeeId = 1L;
        RequestStatus newStatus = RequestStatus.IN_PROGRESS;

        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName("Иван");
        employee.setLastName("Петров");

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(requestRepository.save(any(ClientRequest.class))).thenReturn(request);

        // When
        ClientRequestDto result = clientRequestService.updateStatus(requestId, newStatus, employeeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(request.getEmployee()).isEqualTo(employee);
        verify(employeeRepository).findById(employeeId);
        verify(requestRepository).save(request);
    }

    @Test
    void updatePriority_WithValidData_ShouldUpdatePriority() {
        // Given
        Long requestId = 1L;
        RequestPriority newPriority = RequestPriority.HIGH;

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(ClientRequest.class))).thenReturn(request);

        // When
        ClientRequestDto result = clientRequestService.updatePriority(requestId, newPriority, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(request.getPriority()).isEqualTo(newPriority);
        verify(requestRepository).findById(requestId);
        verify(requestRepository).save(request);
        verify(historyService).logChange(any(), isNull(), eq("PRIORITY"), eq("NORMAL"), eq("HIGH"), anyString());
    }

    @Test
    void updatePriority_WithNonExistentId_ShouldThrowException() {
        // Given
        Long nonExistentId = 999L;
        when(requestRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> clientRequestService.updatePriority(nonExistentId, RequestPriority.HIGH, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request not found: 999");
        verify(requestRepository, never()).save(any(ClientRequest.class));
    }

    @Test
    void findByStatus_WithValidStatus_ShouldReturnRequests() {
        // Given
        RequestStatus status = RequestStatus.NEW;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClientRequest> requestPage = new PageImpl<>(List.of(request));

        when(requestRepository.findByStatus(status, pageable)).thenReturn(requestPage);

        // When
        Page<ClientRequestDto> result = clientRequestService.findByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(requestRepository).findByStatus(status, pageable);
    }

    @Test
    void findByStatusAndPriority_WithValidParams_ShouldReturnRequests() {
        // Given
        RequestStatus status = RequestStatus.NEW;
        RequestPriority priority = RequestPriority.HIGH;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClientRequest> requestPage = new PageImpl<>(List.of(request));

        when(requestRepository.findByStatusAndPriority(status, priority, pageable))
                .thenReturn(requestPage);

        // When
        Page<ClientRequestDto> result = clientRequestService.findByStatusAndPriority(status, priority, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(requestRepository).findByStatusAndPriority(status, priority, pageable);
    }
}

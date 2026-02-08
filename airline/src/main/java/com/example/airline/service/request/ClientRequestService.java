package com.example.airline.service.request;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.entity.client.Client;
import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestPriority;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.entity.tour.Tour;
import com.example.airline.entity.user.Employee;
import com.example.airline.mapper.request.ClientRequestMapper;
import com.example.airline.repository.client.ClientRepository;
import com.example.airline.repository.tour.ClientRequestRepository;
import com.example.airline.repository.user.EmployeeRepository;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.repository.user.UserRepository;
import com.example.airline.service.notification.EmailService;
import com.example.airline.util.CustomValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
public class ClientRequestService {
    private final ClientRequestRepository requestRepository;
    private final TourRepository tourRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final RequestHistoryService historyService;
    private final CustomValidator customValidator;
    private final EmailService emailService;

    public ClientRequestService(ClientRequestRepository requestRepository,
                               TourRepository tourRepository,
                               EmployeeRepository employeeRepository,
                               ClientRepository clientRepository,
                               UserRepository userRepository,
                               RequestHistoryService historyService,
                               CustomValidator customValidator,
                               EmailService emailService) {
        this.requestRepository = requestRepository;
        this.tourRepository = tourRepository;
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
        this.customValidator = customValidator;
        this.emailService = emailService;
    }

    public ClientRequestDto createRequest(ClientRequestDto dto) {
        customValidator.validate(dto);

        Tour tour = tourRepository.findById(dto.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + dto.getTourId()));

        // Проверяем, есть ли клиент с таким email
        Client client = null;
        if (dto.getUserEmail() != null) {
            client = clientRepository.findByEmail(dto.getUserEmail()).orElse(null);
        }

        ClientRequest request = new ClientRequest(
                tour,
                dto.getUserName(),
                dto.getUserEmail(),
                dto.getUserPhone(),
                dto.getComment()
        );
        
        if (client != null) {
            request.setClient(client);
        }
        
        // Определяем приоритет заявки
        RequestPriority priority = determinePriority(dto, tour, client);
        request.setPriority(priority);

        request = requestRepository.save(request);
        
        // Логируем создание заявки
        historyService.logChange(request, null, "STATUS", null, request.getStatus().name(), "Заявка создана");
        
        // Отправляем email-уведомление клиенту
        emailService.sendRequestCreatedNotification(request);

        return ClientRequestMapper.toDto(request);
    }
    
    /**
     * Создание заявки от авторизованного пользователя.
     * Автоматически связывает заявку с клиентом по username.
     */
    public ClientRequestDto createRequestForUser(ClientRequestDto dto, String username) {
        customValidator.validate(dto);
        
        Tour tour = tourRepository.findById(dto.getTourId())
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + dto.getTourId()));
        
        // Находим клиента по username (через User)
        Client client = userRepository.findByUsername(username)
                .flatMap(user -> clientRepository.findByUserId(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Client not found for user: " + username));
        
        // Используем данные из профиля клиента
        ClientRequest request = new ClientRequest(
                tour,
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                dto.getComment()
        );
        
        request.setClient(client);
        
        // Определяем приоритет заявки
        RequestPriority priority = determinePriority(dto, tour, client);
        request.setPriority(priority);
        
        request = requestRepository.save(request);
        
        // Логируем создание заявки
        historyService.logChange(request, null, "STATUS", null, request.getStatus().name(), "Заявка создана клиентом " + client.getFullName());
        
        // Отправляем email-уведомление клиенту
        emailService.sendRequestCreatedNotification(request);
        
        return ClientRequestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public Optional<ClientRequestDto> findById(Long id) {
        ClientRequest request = requestRepository.findByIdWithTour(id);
        return Optional.ofNullable(request).map(ClientRequestMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable)
                .map(ClientRequestMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByStatus(RequestStatus status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable)
                .map(ClientRequestMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByTourId(Long tourId, Pageable pageable) {
        return requestRepository.findByTourId(tourId, pageable)
                .map(ClientRequestMapper::toDto);
    }

    public ClientRequestDto updateStatus(Long id, RequestStatus status) {
        return updateStatus(id, status, null);
    }
    
    public ClientRequestDto updateStatus(Long id, RequestStatus status, Long employeeId) {
        return updateStatus(id, status, employeeId, null);
    }
    
    public ClientRequestDto updateStatus(Long id, RequestStatus status, Long employeeId, Long changedByEmployeeId) {
        ClientRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
        
        RequestStatus oldStatus = request.getStatus();
        Employee oldEmployee = request.getEmployee();
        
        // Обновляем статус
        if (!oldStatus.equals(status)) {
            request.setStatus(status);
            Employee changedBy = changedByEmployeeId != null 
                    ? employeeRepository.findById(changedByEmployeeId).orElse(null)
                    : null;
            historyService.logChange(request, changedBy, "STATUS", oldStatus.name(), status.name(), 
                    "Статус изменен с " + oldStatus + " на " + status);
            
            // Отправляем email-уведомление клиенту об изменении статуса
            emailService.sendStatusChangedNotification(request, oldStatus);
        }
        
        // Обновляем назначенного сотрудника
        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
            
            if (oldEmployee == null || !oldEmployee.getId().equals(employeeId)) {
                String oldEmployeeName = oldEmployee != null ? oldEmployee.getFullName() : null;
                request.setEmployee(employee);
                Employee changedBy = changedByEmployeeId != null 
                        ? employeeRepository.findById(changedByEmployeeId).orElse(null)
                        : null;
                historyService.logChange(request, changedBy, "EMPLOYEE", oldEmployeeName, employee.getFullName(),
                        "Сотрудник назначен: " + employee.getFullName());
                
                // Отправляем email-уведомление сотруднику о назначении заявки
                emailService.sendRequestAssignedNotification(request, employee);
            }
        }
        
        request = requestRepository.save(request);
        
        return ClientRequestMapper.toDto(request);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByClientId(Long clientId, Pageable pageable) {
        return requestRepository.findByClientId(clientId, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByEmployeeId(Long employeeId, Pageable pageable) {
        return requestRepository.findByEmployeeId(employeeId, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByEmployeeIdAndStatus(Long employeeId, RequestStatus status, Pageable pageable) {
        return requestRepository.findByEmployeeIdAndStatus(employeeId, status, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByPriority(RequestPriority priority, Pageable pageable) {
        return requestRepository.findByPriority(priority, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByStatusAndPriority(RequestStatus status, RequestPriority priority, Pageable pageable) {
        return requestRepository.findByStatusAndPriority(status, priority, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findByDateRange(String startDateStr, String endDateStr, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);
        
        return requestRepository.findByDateRange(startDate, endDate, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    public ClientRequestDto updatePriority(Long id, RequestPriority priority, Long changedByEmployeeId) {
        ClientRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
        
        RequestPriority oldPriority = request.getPriority();
        
        if (!oldPriority.equals(priority)) {
            request.setPriority(priority);
            Employee changedBy = changedByEmployeeId != null 
                    ? employeeRepository.findById(changedByEmployeeId).orElse(null)
                    : null;
            historyService.logChange(request, changedBy, "PRIORITY", oldPriority.name(), priority.name(),
                    "Приоритет изменен с " + oldPriority.getDisplayName() + " на " + priority.getDisplayName());
            
            request = requestRepository.save(request);
        }
        
        return ClientRequestMapper.toDto(request);
    }
    
    /**
     * Получить доступные заявки для сотрудников (без назначенного сотрудника)
     */
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findAvailableRequests(Pageable pageable) {
        return requestRepository.findByEmployeeIsNull(pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    /**
     * Получить доступные заявки для сотрудников по статусу
     */
    @Transactional(readOnly = true)
    public Page<ClientRequestDto> findAvailableRequestsByStatus(RequestStatus status, Pageable pageable) {
        return requestRepository.findByEmployeeIsNullAndStatus(status, pageable)
                .map(ClientRequestMapper::toDto);
    }
    
    /**
     * Взять заявку в работу (назначить сотрудника и установить статус IN_PROGRESS)
     */
    public ClientRequestDto takeRequest(Long requestId, Long employeeId) {
        ClientRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        
        // Проверяем, что заявка еще не назначена на сотрудника
        if (request.getEmployee() != null) {
            throw new IllegalArgumentException("Request is already assigned to employee: " + request.getEmployee().getFullName());
        }
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        
        // Назначаем сотрудника
        request.setEmployee(employee);
        
        // Если статус не IN_PROGRESS, меняем на IN_PROGRESS
        RequestStatus oldStatus = request.getStatus();
        if (oldStatus != RequestStatus.IN_PROGRESS) {
            request.setStatus(RequestStatus.IN_PROGRESS);
            historyService.logChange(request, employee, "STATUS", oldStatus.name(), RequestStatus.IN_PROGRESS.name(),
                    "Заявка взята в работу сотрудником " + employee.getFullName());
        }
        
        historyService.logChange(request, employee, "EMPLOYEE", null, employee.getFullName(),
                "Сотрудник взял заявку в работу: " + employee.getFullName());
        
        request = requestRepository.save(request);
        
        // Отправляем email-уведомление сотруднику о назначении заявки
        emailService.sendRequestAssignedNotification(request, employee);
        
        return ClientRequestMapper.toDto(request);
    }
    
    /**
     * Обновить статус заявки сотрудником (только для его заявок)
     */
    public ClientRequestDto updateStatusByEmployee(Long requestId, RequestStatus status, Long employeeId) {
        ClientRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        
        // Проверяем, что заявка назначена на этого сотрудника
        if (request.getEmployee() == null || !request.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You are not assigned to this request");
        }
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        
        RequestStatus oldStatus = request.getStatus();
        
        if (!oldStatus.equals(status)) {
            request.setStatus(status);
            historyService.logChange(request, employee, "STATUS", oldStatus.name(), status.name(),
                    "Статус изменен сотрудником с " + oldStatus + " на " + status);
            
            // Отправляем email-уведомление клиенту об изменении статуса
            emailService.sendStatusChangedNotification(request, oldStatus);
            
            request = requestRepository.save(request);
        }
        
        return ClientRequestMapper.toDto(request);
    }
    
    /**
     * Автоматическое определение приоритета заявки на основе различных факторов
     */
    private RequestPriority determinePriority(ClientRequestDto dto, Tour tour, Client client) {
        // Если приоритет указан явно в DTO, используем его
        if (dto.getPriority() != null) {
            return dto.getPriority();
        }
        
        // VIP клиент -> HIGH приоритет
        if (client != null && Boolean.TRUE.equals(client.getVipStatus())) {
            return RequestPriority.HIGH;
        }
        
        // Дорогой тур (более 100000) -> HIGH приоритет
        if (tour.getPrice() != null && tour.getPrice().compareTo(java.math.BigDecimal.valueOf(100000)) > 0) {
            return RequestPriority.HIGH;
        }
        
        // По умолчанию NORMAL
        return RequestPriority.NORMAL;
    }
}


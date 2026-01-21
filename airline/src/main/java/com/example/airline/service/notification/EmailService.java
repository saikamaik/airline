package com.example.airline.service.notification;

import com.example.airline.entity.tour.ClientRequest;
import com.example.airline.entity.tour.RequestStatus;
import com.example.airline.entity.user.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.email.from:noreply@airline.com}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Отправка уведомления клиенту о создании заявки
     */
    @Async
    public void sendRequestCreatedNotification(ClientRequest request) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled. Skipping notification for request {}", request.getId());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getUserEmail());
            message.setSubject("Ваша заявка на тур принята");
            message.setText(buildRequestCreatedMessage(request));
            
            mailSender.send(message);
            logger.info("Email notification sent to {} for request {}", request.getUserEmail(), request.getId());
        } catch (Exception e) {
            logger.error("Failed to send email notification for request {}", request.getId(), e);
        }
    }
    
    /**
     * Отправка уведомления клиенту об изменении статуса заявки
     */
    @Async
    public void sendStatusChangedNotification(ClientRequest request, RequestStatus oldStatus) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled. Skipping notification for request {}", request.getId());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getUserEmail());
            message.setSubject("Изменение статуса вашей заявки");
            message.setText(buildStatusChangedMessage(request, oldStatus));
            
            mailSender.send(message);
            logger.info("Status change email sent to {} for request {}", request.getUserEmail(), request.getId());
        } catch (Exception e) {
            logger.error("Failed to send status change email for request {}", request.getId(), e);
        }
    }
    
    /**
     * Отправка уведомления сотруднику о назначении новой заявки
     */
    @Async
    public void sendRequestAssignedNotification(ClientRequest request, Employee employee) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled. Skipping notification for employee {}", employee.getId());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Вам назначена новая заявка");
            message.setText(buildRequestAssignedMessage(request, employee));
            
            mailSender.send(message);
            logger.info("Assignment email sent to {} for request {}", employee.getEmail(), request.getId());
        } catch (Exception e) {
            logger.error("Failed to send assignment email for request {}", request.getId(), e);
        }
    }
    
    /**
     * Отправка напоминания сотруднику о необработанных заявках
     */
    @Async
    public void sendReminderNotification(Employee employee, int unprocessedCount) {
        if (!emailEnabled) {
            logger.debug("Email notifications are disabled. Skipping reminder for employee {}", employee.getId());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Напоминание о необработанных заявках");
            message.setText(buildReminderMessage(employee, unprocessedCount));
            
            mailSender.send(message);
            logger.info("Reminder email sent to {} about {} unprocessed requests", employee.getEmail(), unprocessedCount);
        } catch (Exception e) {
            logger.error("Failed to send reminder email to employee {}", employee.getId(), e);
        }
    }
    
    private String buildRequestCreatedMessage(ClientRequest request) {
        return String.format(
            "Здравствуйте, %s!\n\n" +
            "Ваша заявка на тур \"%s\" успешно принята.\n\n" +
            "Детали заявки:\n" +
            "- Номер заявки: #%d\n" +
            "- Тур: %s\n" +
            "- Статус: %s\n" +
            "- Дата создания: %s\n\n" +
            "Мы свяжемся с вами в ближайшее время для уточнения деталей.\n\n" +
            "С уважением,\n" +
            "Команда турагентства",
            request.getUserName(),
            request.getTour().getName(),
            request.getId(),
            request.getTour().getName(),
            request.getStatus().getDisplayName(),
            request.getCreatedAt()
        );
    }
    
    private String buildStatusChangedMessage(ClientRequest request, RequestStatus oldStatus) {
        return String.format(
            "Здравствуйте, %s!\n\n" +
            "Статус вашей заявки #%d изменен.\n\n" +
            "Детали:\n" +
            "- Тур: %s\n" +
            "- Предыдущий статус: %s\n" +
            "- Новый статус: %s\n" +
            "- Дата изменения: %s\n\n" +
            "%s\n\n" +
            "С уважением,\n" +
            "Команда турагентства",
            request.getUserName(),
            request.getId(),
            request.getTour().getName(),
            oldStatus.getDisplayName(),
            request.getStatus().getDisplayName(),
            java.time.LocalDateTime.now(),
            getStatusMessage(request.getStatus())
        );
    }
    
    private String buildRequestAssignedMessage(ClientRequest request, Employee employee) {
        return String.format(
            "Здравствуйте, %s!\n\n" +
            "Вам назначена новая заявка для обработки.\n\n" +
            "Детали заявки:\n" +
            "- Номер заявки: #%d\n" +
            "- Клиент: %s\n" +
            "- Email клиента: %s\n" +
            "- Телефон: %s\n" +
            "- Тур: %s\n" +
            "- Статус: %s\n" +
            "- Дата создания: %s\n\n" +
            "%s\n\n" +
            "Пожалуйста, обработайте заявку в ближайшее время.\n\n" +
            "С уважением,\n" +
            "Система управления заявками",
            employee.getFullName(),
            request.getId(),
            request.getUserName(),
            request.getUserEmail(),
            request.getUserPhone() != null ? request.getUserPhone() : "не указан",
            request.getTour().getName(),
            request.getStatus().getDisplayName(),
            request.getCreatedAt(),
            request.getComment() != null ? "Комментарий клиента: " + request.getComment() : ""
        );
    }
    
    private String buildReminderMessage(Employee employee, int unprocessedCount) {
        return String.format(
            "Здравствуйте, %s!\n\n" +
            "Напоминаем, что у вас есть %d необработанных заявок.\n\n" +
            "Пожалуйста, проверьте список заявок и обработайте их в ближайшее время.\n\n" +
            "С уважением,\n" +
            "Система управления заявками",
            employee.getFullName(),
            unprocessedCount
        );
    }
    
    private String getStatusMessage(RequestStatus status) {
        return switch (status) {
            case NEW -> "Ваша заявка принята и ожидает обработки.";
            case IN_PROGRESS -> "Ваша заявка находится в обработке. Мы свяжемся с вами в ближайшее время.";
            case COMPLETED -> "Ваша заявка успешно обработана. Спасибо за выбор нашего турагентства!";
            case CANCELLED -> "Ваша заявка была отменена. Если у вас есть вопросы, пожалуйста, свяжитесь с нами.";
        };
    }
}


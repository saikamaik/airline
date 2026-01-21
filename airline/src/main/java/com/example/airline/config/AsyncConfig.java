package com.example.airline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Конфигурация асинхронного выполнения задач.
 * Включает поддержку асинхронных методов, помеченных аннотацией @Async.
 * 
 * Используется для асинхронной отправки email-уведомлений,
 * чтобы не блокировать основной поток выполнения при взаимодействии с SMTP-сервером.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Конфигурация по умолчанию использует SimpleAsyncTaskExecutor
    // Для production-среды рекомендуется настроить ThreadPoolTaskExecutor
}

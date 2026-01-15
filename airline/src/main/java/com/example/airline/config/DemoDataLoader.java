package com.example.airline.config;

import com.example.airline.entity.flight.Aircraft;
import com.example.airline.entity.flight.Model;
import com.example.airline.entity.flight.Airport;
import com.example.airline.entity.flight.LocalizedAirportName;
import com.example.airline.entity.flight.LocalizedCityName;
import com.example.airline.entity.flight.Flight;
import com.example.airline.entity.flight.Status;
import com.example.airline.entity.tour.Tour;
import com.example.airline.entity.user.Role;
import com.example.airline.entity.user.RoleName;
import com.example.airline.entity.user.User;
import com.example.airline.repository.flight.AircraftRepository;
import com.example.airline.repository.flight.AirportRepository;
import com.example.airline.repository.flight.FlightRepository;
import com.example.airline.repository.user.RoleRepository;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Небольшой загрузчик демонстрационных данных для локального запуска приложения.
 * Не выполняется, если в таблице рейсов уже есть записи.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    @Value("${app.demo-data.enabled:true}")
    private boolean demoDataEnabled;

    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(AirportRepository airportRepository,
                          AircraftRepository aircraftRepository,
                          FlightRepository flightRepository,
                          TourRepository tourRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.airportRepository = airportRepository;
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!demoDataEnabled) {
            System.out.println("=== DemoDataLoader: Отключен через настройки ===");
            return;
        }
        
        System.out.println("=== DemoDataLoader: Проверка данных ===");
        boolean dataExists = flightRepository.count() > 0;
        
        // Всегда обновляем пароли пользователей, даже если данные уже есть
        updateDemoUsers();
        
        // Всегда создаем/обновляем демо-туры, даже если данные уже есть
        if (dataExists) {
            System.out.println("=== DemoDataLoader: Данные уже существуют, обновление туров ===");
            try {
                updateDemoTours();
            } catch (Exception e) {
                System.out.println("=== DemoDataLoader: Ошибка при обновлении туров (возможно, таблица еще не создана): " + e.getMessage());
            }
            return;
        }
        System.out.println("=== DemoDataLoader: Начало загрузки демо-данных ===");

        Airport svo = new Airport(
                "SVO",
                new LocalizedAirportName("Sheremetyevo International Airport", "Шереметьево"),
                new LocalizedCityName("Moscow", "Москва"),
                ZoneId.of("Europe/Moscow")
        );
        Airport led = new Airport(
                "LED",
                new LocalizedAirportName("Pulkovo Airport", "Пулково"),
                new LocalizedCityName("Saint Petersburg", "Санкт‑Петербург"),
                ZoneId.of("Europe/Moscow")
        );
        Airport hkt = new Airport(
                "HKT",
                new LocalizedAirportName("Phuket International Airport", "Пхукет"),
                new LocalizedCityName("Phuket", "Пхукет"),
                ZoneId.of("Asia/Bangkok")
        );

        airportRepository.saveAll(List.of(svo, led, hkt));
        System.out.println("=== DemoDataLoader: Аэропорты созданы ===");

        Aircraft airbus320 = new Aircraft("320",
                new Model("Airbus A320", "Airbus A320"),
                6100);
        Aircraft boeing777 = new Aircraft("77W",
                new Model("Boeing 777-300ER", "Боинг 777-300ER"),
                13650);

        aircraftRepository.saveAll(List.of(airbus320, boeing777));
        System.out.println("=== DemoDataLoader: Самолёты созданы ===");

        System.out.println("=== DemoDataLoader: Самолеты созданы ===");

        Flight moscowToSpb = new Flight.Builder()
                .flightNumber("SU123")
                .scheduledDeparture(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30))
                .scheduledArrival(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .departureAirport(svo)
                .arrivalAirport(led)
                .status(Status.SCHEDULED)
                .aircraftCode(airbus320)
                .build();

        Flight moscowToPhuket = new Flight.Builder()
                .flightNumber("SU6001")
                .scheduledDeparture(LocalDateTime.now().plusDays(5).withHour(23).withMinute(45))
                .scheduledArrival(LocalDateTime.now().plusDays(6).withHour(9).withMinute(10))
                .departureAirport(svo)
                .arrivalAirport(hkt)
                .status(Status.SCHEDULED)
                .aircraftCode(boeing777)
                .build();

        flightRepository.saveAll(List.of(moscowToSpb, moscowToPhuket));
        System.out.println("=== DemoDataLoader: Рейсы созданы ===");

        // Create demo tours
        Tour phuketTour = new Tour(
                "Пляжный отдых на Пхукете",
                "Незабываемый отпуск на тропическом острове Пхукет с посещением лучших пляжей, " +
                "экскурсиями по достопримечательностям и отдыхом в комфортабельном отеле.",
                new BigDecimal("89900.00"),
                10,
                "https://example.com/phuket.jpg",
                "Пхукет"
        );
        Set<Flight> phuketFlights = new HashSet<>();
        phuketFlights.add(moscowToPhuket);
        phuketTour.setFlights(phuketFlights);

        Tour spbTour = new Tour(
                "Экскурсионный тур в Санкт-Петербург",
                "Культурная программа по северной столице: Эрмитаж, Петергоф, " +
                "прогулки по Невскому проспекту и каналам города.",
                new BigDecimal("25000.00"),
                3,
                "https://example.com/spb.jpg",
                "Санкт-Петербург"
        );
        Set<Flight> spbFlights = new HashSet<>();
        spbFlights.add(moscowToSpb);
        spbTour.setFlights(spbFlights);

        tourRepository.saveAll(List.of(phuketTour, spbTour));
        System.out.println("=== DemoDataLoader: Туры созданы ===");

        // Create roles if they don't exist
        System.out.println("=== DemoDataLoader: Создание ролей ===");
        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));
        System.out.println("=== DemoDataLoader: Роли созданы ===");

        // Update demo users
        updateDemoUsers();
        System.out.println("=== DemoDataLoader: Загрузка завершена! ===");
    }

    private void updateDemoTours() {
        System.out.println("=== DemoDataLoader: Обновление демо-туров ===");
        
        // Проверяем, что таблица туров существует
        try {
            long tourCount = tourRepository.count();
            System.out.println("=== DemoDataLoader: В базе найдено туров: " + tourCount);
        } catch (Exception e) {
            System.out.println("=== DemoDataLoader: Таблица tours еще не создана, пропуск обновления туров");
            return;
        }
        
        // Находим рейсы для туров
        List<Flight> allFlights = flightRepository.findAll();
        Flight moscowToPhuket = allFlights.stream()
                .filter(f -> f.getArrivalAirport() != null && "HKT".equals(f.getArrivalAirport().getAirportCode()))
                .findFirst()
                .orElse(null);
        Flight moscowToSpb = allFlights.stream()
                .filter(f -> f.getArrivalAirport() != null && "LED".equals(f.getArrivalAirport().getAirportCode()))
                .findFirst()
                .orElse(null);

        // Создаем или обновляем тур на Пхукет
        Tour phuketTour = tourRepository.findByDestinationCity("Пхукет")
                .stream()
                .findFirst()
                .orElse(null);
        
        if (phuketTour == null) {
            phuketTour = new Tour(
                    "Пляжный отдых на Пхукете",
                    "Незабываемый отпуск на тропическом острове Пхукет с посещением лучших пляжей, " +
                    "экскурсиями по достопримечательностям и отдыхом в комфортабельном отеле.",
                    new BigDecimal("89900.00"),
                    10,
                    "https://example.com/phuket.jpg",
                    "Пхукет"
            );
        } else {
            phuketTour.setName("Пляжный отдых на Пхукете");
            phuketTour.setDescription("Незабываемый отпуск на тропическом острове Пхукет с посещением лучших пляжей, " +
                    "экскурсиями по достопримечательностям и отдыхом в комфортабельном отеле.");
            phuketTour.setPrice(new BigDecimal("89900.00"));
            phuketTour.setDurationDays(10);
            phuketTour.setImageUrl("https://example.com/phuket.jpg");
            phuketTour.setActive(true);
        }
        if (moscowToPhuket != null) {
            Set<Flight> phuketFlights = new HashSet<>();
            phuketFlights.add(moscowToPhuket);
            phuketTour.setFlights(phuketFlights);
        }
        tourRepository.save(phuketTour);
        System.out.println("=== DemoDataLoader: Тур на Пхукет обновлён ===");

        // Создаем или обновляем тур в Санкт-Петербург
        Tour spbTour = tourRepository.findByDestinationCity("Санкт-Петербург")
                .stream()
                .findFirst()
                .orElse(null);
        
        if (spbTour == null) {
            spbTour = new Tour(
                    "Экскурсионный тур в Санкт-Петербург",
                    "Культурная программа по северной столице: Эрмитаж, Петергоф, " +
                    "прогулки по Невскому проспекту и каналам города.",
                    new BigDecimal("25000.00"),
                    3,
                    "https://example.com/spb.jpg",
                    "Санкт-Петербург"
            );
        } else {
            spbTour.setName("Экскурсионный тур в Санкт-Петербург");
            spbTour.setDescription("Культурная программа по северной столице: Эрмитаж, Петергоф, " +
                    "прогулки по Невскому проспекту и каналам города.");
            spbTour.setPrice(new BigDecimal("25000.00"));
            spbTour.setDurationDays(3);
            spbTour.setImageUrl("https://example.com/spb.jpg");
            spbTour.setActive(true);
        }
        if (moscowToSpb != null) {
            Set<Flight> spbFlights = new HashSet<>();
            spbFlights.add(moscowToSpb);
            spbTour.setFlights(spbFlights);
        }
        tourRepository.save(spbTour);
        System.out.println("=== DemoDataLoader: Тур в Санкт-Петербург обновлён ===");
    }

    private void updateDemoUsers() {
        System.out.println("=== DemoDataLoader: Обновление демо-пользователей ===");
        
        // Create roles if they don't exist
        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        // Create or update demo users
        User user = userRepository.findByUsername("user").orElse(null);
        if (user == null) {
            user = new User("user", passwordEncoder.encode("password123"), "user@example.com");
            user.getRoles().add(roleUser);
            userRepository.save(user);
            System.out.println("=== DemoDataLoader: Пользователь 'user' создан ===");
        } else {
            // Обновляем пароль на случай, если он был изменен
            String newPassword = passwordEncoder.encode("password123");
            user.setPassword(newPassword);
            if (!user.getRoles().contains(roleUser)) {
                user.getRoles().add(roleUser);
            }
            userRepository.save(user);
            System.out.println("=== DemoDataLoader: Пользователь 'user' обновлён (пароль сброшен на password123) ===");
        }

        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User("admin", passwordEncoder.encode("password123"), "admin@example.com");
            admin.getRoles().add(roleUser);
            admin.getRoles().add(roleAdmin);
            userRepository.save(admin);
            System.out.println("=== DemoDataLoader: Пользователь 'admin' создан ===");
        } else {
            // Обновляем пароль на случай, если он был изменен
            String newPassword = passwordEncoder.encode("password123");
            admin.setPassword(newPassword);
            if (!admin.getRoles().contains(roleUser)) {
                admin.getRoles().add(roleUser);
            }
            if (!admin.getRoles().contains(roleAdmin)) {
                admin.getRoles().add(roleAdmin);
            }
            userRepository.save(admin);
            System.out.println("=== DemoDataLoader: Пользователь 'admin' обновлён (пароль сброшен на password123) ===");
        }
        System.out.println("=== DemoDataLoader: Пользователи обновлены ===");
    }

}




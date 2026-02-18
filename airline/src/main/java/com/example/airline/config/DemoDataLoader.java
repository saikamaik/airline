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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DemoDataLoader.class);

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
            return;
        }
        
        boolean dataExists = flightRepository.count() > 0;
        updateDemoUsers();
        
        if (dataExists) {
            try {
                updateDemoTours();
            } catch (Exception e) {
                logger.warn("Failed to update demo tours: {}", e.getMessage());
            }
            return;
        }

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

        Aircraft airbus320 = new Aircraft("320",
                new Model("Airbus A320", "Airbus A320"),
                6100);
        Aircraft boeing777 = new Aircraft("77W",
                new Model("Boeing 777-300ER", "Боинг 777-300ER"),
                13650);

        aircraftRepository.saveAll(List.of(airbus320, boeing777));

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

        roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        updateDemoUsers();
    }

    private void updateDemoTours() {
        try {
            tourRepository.count();
        } catch (Exception e) {
            return;
        }
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
    }

    private void updateDemoUsers() {
        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        User user = userRepository.findByUsername("user").orElse(null);
        if (user == null) {
            user = new User("user", passwordEncoder.encode("password123"), "user@example.com");
            user.getRoles().add(roleUser);
        } else {
            user.setPassword(passwordEncoder.encode("password123"));
            if (!user.getRoles().contains(roleUser)) {
                user.getRoles().add(roleUser);
            }
        }
        userRepository.save(user);

        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User("admin", passwordEncoder.encode("password123"), "admin@example.com");
            admin.getRoles().add(roleUser);
            admin.getRoles().add(roleAdmin);
        } else {
            admin.setPassword(passwordEncoder.encode("password123"));
            if (!admin.getRoles().contains(roleUser)) {
                admin.getRoles().add(roleUser);
            }
            if (!admin.getRoles().contains(roleAdmin)) {
                admin.getRoles().add(roleAdmin);
            }
        }
        userRepository.save(admin);
    }

}




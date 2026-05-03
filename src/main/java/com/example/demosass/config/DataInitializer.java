package com.example.demosass.config;

import com.example.demosass.domain.enums.DayOfWeek;
import com.example.demosass.domain.enums.Role;
import com.example.demosass.domain.model.*;
import com.example.demosass.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Base de datos ya inicializada, saltando seed...");
            return;
        }

        log.info("Inicializando datos de ServiLink...");

        Category electricidad = categoryRepository.save(Category.builder()
            .name("Electricidad").description("Instalaciones y reparaciones eléctricas").build());
        Category plomeria = categoryRepository.save(Category.builder()
            .name("Plomería").description("Gasfitería y sistemas de agua").build());
        Category limpieza = categoryRepository.save(Category.builder()
            .name("Limpieza").description("Limpieza de hogar y oficinas").build());
        Category jardineria = categoryRepository.save(Category.builder()
            .name("Jardinería").description("Mantenimiento de jardines").build());

        Service instElectrica = serviceRepository.save(Service.builder()
            .name("Instalación eléctrica").description("Instalación de tomacorrientes y llaves")
            .referencePrice(BigDecimal.valueOf(80)).estimatedDurationHours(2).category(electricidad).build());
        Service repPlomeria = serviceRepository.save(Service.builder()
            .name("Reparación de tuberías").description("Detección y arreglo de fugas")
            .referencePrice(BigDecimal.valueOf(100)).estimatedDurationHours(3).category(plomeria).build());
        Service limpiezaHogar = serviceRepository.save(Service.builder()
            .name("Limpieza de hogar").description("Limpieza completa de departamento o casa")
            .referencePrice(BigDecimal.valueOf(60)).estimatedDurationHours(4).category(limpieza).build());

        User u1 = userRepository.save(User.builder()
            .name("Juan Ríos Suárez").email("juan.rios@servilink.pe")
            .password(passwordEncoder.encode("password123"))
            .phone("987654321").role(Role.PROFESSIONAL).build());

        Professional p1 = professionalRepository.save(Professional.builder()
            .user(u1).specialty("Electricista Certificado")
            .description("10 años de experiencia en instalaciones residenciales y comerciales")
            .latitude(-12.0464).longitude(-77.0428)  // Miraflores, Lima
            .address("Av. Larco 345, Miraflores")
            .coverageRadiusKm(8.0).baseRate(BigDecimal.valueOf(50))
            .isVerified(true).averageRating(4.9).totalReviews(47)
            .services(List.of(instElectrica)).build());

        User u2 = userRepository.save(User.builder()
            .name("María Condori Paz").email("maria.condori@servilink.pe")
            .password(passwordEncoder.encode("password123"))
            .phone("976543210").role(Role.PROFESSIONAL).build());

        Professional p2 = professionalRepository.save(Professional.builder()
            .user(u2).specialty("Gasfitería y Plomería")
            .description("Especialista en tuberías PVC y cobre, emergencias 24/7")
            .latitude(-12.0700).longitude(-77.0500)  // Surquillo, Lima
            .address("Jr. Tomas Marsano 200, Surquillo")
            .coverageRadiusKm(10.0).baseRate(BigDecimal.valueOf(40))
            .isVerified(true).averageRating(4.6).totalReviews(32)
            .services(List.of(repPlomeria)).build());

        User u3 = userRepository.save(User.builder()
            .name("Luis Paredes Torres").email("luis.paredes@servilink.pe")
            .password(passwordEncoder.encode("password123"))
            .phone("965432109").role(Role.PROFESSIONAL).build());

        professionalRepository.save(Professional.builder()
            .user(u3).specialty("Limpieza Profesional")
            .description("Servicio de limpieza profunda con productos ecológicos")
            .latitude(-12.0900).longitude(-77.0600)  // San Borja, Lima
            .address("Av. Angamos Este 500, San Borja")
            .coverageRadiusKm(12.0).baseRate(BigDecimal.valueOf(35))
            .isVerified(false).averageRating(4.4).totalReviews(18)
            .services(List.of(limpiezaHogar)).build());

        userRepository.save(User.builder()
            .name("Carlos Mendoza").email("carlos@servilink.pe")
            .password(passwordEncoder.encode("password123"))
            .phone("954321098").role(Role.CLIENT).build());

        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                      DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            availabilityRepository.save(Availability.builder()
                .professional(p1).dayOfWeek(day)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(18, 0))
                .isAvailable(true).build());
        }
        availabilityRepository.save(Availability.builder()
            .professional(p1).dayOfWeek(DayOfWeek.SATURDAY)
            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(14, 0))
            .isAvailable(true).build());

        log.info("✅ Datos iniciales cargados: {} categorías, {} servicios, {} profesionales",
            categoryRepository.count(), serviceRepository.count(), professionalRepository.count());
    }
}

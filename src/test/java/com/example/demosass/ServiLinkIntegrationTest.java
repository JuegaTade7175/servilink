package com.example.demosass;

import com.example.demosass.domain.enums.Role;
import com.example.demosass.dto.request.AuthRequest.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiLinkIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String clientToken;
    private static String professionalToken;
    private static Long createdBookingId;
    private static Long createdProfessionalId;

    @Test @Order(1)
    void registrarClienteExitoso() throws Exception {
        RegisterRequest req = new RegisterRequest(
            "Carlos Test", "carlos.test@servilink.pe", "password123", "999000001", Role.CLIENT
        );
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("CLIENT"))
            .andExpect(jsonPath("$.email").value("carlos.test@servilink.pe"))
            .andReturn();

        clientToken = objectMapper.readTree(
            result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(2)
    void registrarProfesionalExitoso() throws Exception {
        RegisterRequest req = new RegisterRequest(
            "Juan Test Pro", "juan.test@servilink.pe", "password123", "999000002", Role.PROFESSIONAL
        );
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("PROFESSIONAL"))
            .andReturn();

        professionalToken = objectMapper.readTree(
            result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test @Order(3)
    void registrarEmailDuplicadoFalla() throws Exception {
        RegisterRequest req = new RegisterRequest(
            "Duplicado", "carlos.test@servilink.pe", "password123", "999000003", Role.CLIENT
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(4)
    void loginExitoso() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"carlos.test@servilink.pe\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test @Order(5)
    void loginCredencialesIncorrectasFalla() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"carlos.test@servilink.pe\",\"password\":\"wrongpassword\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(6)
    void loginEmailInexistenteFalla() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"noexiste@servilink.pe\",\"password\":\"password123\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(10)
    void verMiPerfilRequiereAuth() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(11)
    void verMiPerfilConToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("carlos.test@servilink.pe"))
            .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test @Order(12)
    void actualizarFotoPerfilConUrlValida() throws Exception {
        mockMvc.perform(patch("/api/users/profile-picture")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profilePictureUrl\":\"https://res.cloudinary.com/demo/image/upload/sample.jpg\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profilePictureUrl").value("https://res.cloudinary.com/demo/image/upload/sample.jpg"));
    }

    @Test @Order(13)
    void actualizarFotoPerfilSinHttpsFalla() throws Exception {
        mockMvc.perform(patch("/api/users/profile-picture")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profilePictureUrl\":\"http://insecure.com/foto.jpg\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(14)
    void eliminarFotoPerfil() throws Exception {
        mockMvc.perform(delete("/api/users/profile-picture")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test @Order(20)
    void listarCategoriasPublico() throws Exception {
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(21)
    void crearCategoriaYVerla() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electricidad Test\",\"description\":\"Servicios eléctricos\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Electricidad Test"))
            .andReturn();

        Long catId = objectMapper.readTree(
            result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/categories/" + catId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Electricidad Test"));
    }

    @Test @Order(22)
    void crearCategoriaDuplicadaFalla() throws Exception {
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electricidad Test\",\"description\":\"duplicada\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(23)
    void agregarServicioACategoria() throws Exception {
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Plomería Test\",\"description\":\"Tuberías\"}"))
            .andExpect(status().isCreated())
            .andReturn();
        Long catId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/categories/" + catId + "/services")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Reparación de tuberías\",\"referencePrice\":90.00,\"estimatedDurationHours\":2}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Reparación de tuberías"));

        mockMvc.perform(get("/api/categories/" + catId + "/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(24)
    void categoriaInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/categories/99999"))
            .andExpect(status().isNotFound());
    }

    @Test @Order(30)
    void crearPerfilProfesionalExitoso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/professionals/profile")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "specialty": "Electricista Certificado",
                      "description": "10 años de experiencia",
                      "latitude": -12.0464,
                      "longitude": -77.0428,
                      "address": "Av. Larco 345, Miraflores",
                      "coverageRadiusKm": 10.0,
                      "baseRate": 50.00
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.specialty").value("Electricista Certificado"))
            .andExpect(jsonPath("$.baseRate").value(50.00))
            .andReturn();

        createdProfessionalId = objectMapper.readTree(
            result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test @Order(31)
    void crearPerfilProfesionalDuplicadoFalla() throws Exception {
        mockMvc.perform(post("/api/professionals/profile")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"specialty\":\"Duplicado\",\"baseRate\":30.00}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(32)
    void verPerfilProfesionalPublico() throws Exception {
        mockMvc.perform(get("/api/professionals/" + createdProfessionalId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.specialty").value("Electricista Certificado"));
    }

    @Test @Order(33)
    void verMiPerfilProfesional() throws Exception {
        mockMvc.perform(get("/api/professionals/me")
                .header("Authorization", "Bearer " + professionalToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.specialty").value("Electricista Certificado"));
    }

    @Test @Order(34)
    void actualizarPerfilProfesional() throws Exception {
        mockMvc.perform(put("/api/professionals/profile")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"baseRate\":65.00,\"coverageRadiusKm\":12.0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.baseRate").value(65.00));
    }

    @Test @Order(35)
    void buscarProfesionalesCercanos() throws Exception {
        mockMvc.perform(get("/api/professionals/nearby")
                .param("lat", "-12.0464")
                .param("lon", "-77.0428")
                .param("radius", "15.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(36)
    void profesionalInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/professionals/99999"))
            .andExpect(status().isNotFound());
    }

    @Test @Order(37)
    void clienteNoPuedeCrearPerfilProfesional() throws Exception {
        mockMvc.perform(post("/api/professionals/profile")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"specialty\":\"Intento\",\"baseRate\":10.00}"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(40)
    void verDisponibilidadPublica() throws Exception {
        mockMvc.perform(get("/api/availability/professional/" + createdProfessionalId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(41)
    void crearDisponibilidadComoProfesional() throws Exception {
        mockMvc.perform(post("/api/availability")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"08:00\",\"endTime\":\"17:00\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
            .andExpect(jsonPath("$.startTime").value("08:00:00"))
            .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test @Order(42)
    void clienteNoPuedeCrearDisponibilidad() throws Exception {
        mockMvc.perform(post("/api/availability")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dayOfWeek\":\"TUESDAY\",\"startTime\":\"09:00\",\"endTime\":\"18:00\"}"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(43)
    void verDisponibilidadPorDia() throws Exception {
        mockMvc.perform(get("/api/availability/professional/" + createdProfessionalId + "/day")
                .param("day", "MONDAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(50)
    void calcularDistanciaHaversine() throws Exception {
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "-12.0464").param("lon1", "-77.0428")
                .param("lat2", "-12.0700").param("lon2", "-77.0500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").isNumber())
            .andExpect(jsonPath("$.distanceM").isNumber());
    }

    @Test @Order(51)
    void obtenerProfesionalesParaMapa() throws Exception {
        mockMvc.perform(get("/api/map/professionals")
                .param("lat", "-12.0464")
                .param("lon", "-77.0428")
                .param("radius", "20.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(60)
    void crearReservaExitosa() throws Exception {
        MvcResult catResult = mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Gasfitería Test\",\"description\":\"desc\"}"))
            .andReturn();
        Long catId = objectMapper.readTree(catResult.getResponse().getContentAsString()).get("id").asLong();

        MvcResult svcResult = mockMvc.perform(post("/api/categories/" + catId + "/services")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Servicio Test\",\"referencePrice\":80.00,\"estimatedDurationHours\":2}"))
            .andReturn();
        Long serviceId = objectMapper.readTree(svcResult.getResponse().getContentAsString()).get("id").asLong();

        String body = String.format("""
            {
              "professionalId": %d,
              "serviceId": %d,
              "scheduledAt": "2027-06-20T10:00:00",
              "address": "Av. Test 123, Lima",
              "clientLatitude": -12.0464,
              "clientLongitude": -77.0428,
              "description": "Necesito reparar una tubería"
            }
            """, createdProfessionalId, serviceId);

        MvcResult result = mockMvc.perform(post("/api/bookings")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.address").value("Av. Test 123, Lima"))
            .andReturn();

        createdBookingId = objectMapper.readTree(
            result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test @Order(61)
    void verMisReservasComoCliente() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(62)
    void verReservasPorProfesional() throws Exception {
        mockMvc.perform(get("/api/bookings/professional")
                .header("Authorization", "Bearer " + clientToken)
                .param("professionalId", createdProfessionalId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(63)
    void verReservaPorId() throws Exception {
        mockMvc.perform(get("/api/bookings/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdBookingId));
    }

    @Test @Order(64)
    void cambiarEstadoReservaAConfirmed() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + createdBookingId + "/status")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CONFIRMED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test @Order(65)
    void reservaRequiereAuth() throws Exception {
        mockMvc.perform(get("/api/bookings/my"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(70)
    void notificacionesRequierenAuth() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(71)
    void verTodasLasNotificaciones() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(72)
    void verNotificacionesNoLeidas() throws Exception {
        mockMvc.perform(get("/api/notifications/unread")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(73)
    void contarNotificacionesNoLeidas() throws Exception {
        mockMvc.perform(get("/api/notifications/unread/count")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").isNumber());
    }

    @Test @Order(74)
    void marcarTodasNotificacionesComoLeidas() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.markedAsRead").isNumber());
    }

    @Test @Order(80)
    void mensajesRequierenAuth() throws Exception {
        mockMvc.perform(get("/api/messages/booking/1"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(81)
    void contarMensajesNoLeidos() throws Exception {
        mockMvc.perform(get("/api/messages/unread/count")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").isNumber());
    }

    @Test @Order(82)
    void enviarMensajeEnReserva() throws Exception {
        mockMvc.perform(post("/api/messages/booking/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Hola, ¿a qué hora llegas?\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Hola, ¿a qué hora llegas?"))
            .andExpect(jsonPath("$.bookingId").value(createdBookingId));
    }

    @Test @Order(83)
    void verConversacionDeReserva() throws Exception {
        mockMvc.perform(get("/api/messages/booking/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test @Order(84)
    void marcarMensajesComoLeidos() throws Exception {
        mockMvc.perform(patch("/api/messages/booking/" + createdBookingId + "/read")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.markedAsRead").isNumber());
    }

    @Test @Order(85)
    void mensajeVacioFalla() throws Exception {
        mockMvc.perform(post("/api/messages/booking/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(90)
    void procesarPagoConTarjeta() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + createdBookingId + "/status")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PENDING\"}"))
            .andReturn();

        String body = String.format("""
            {"bookingId":%d,"amount":80.00,"method":"CARD"}
            """, createdBookingId);

        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.method").value("CARD"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.transactionId").isNotEmpty());
    }

    @Test @Order(91)
    void verPagoDeReserva() throws Exception {
        mockMvc.perform(get("/api/payments/booking/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount").value(80.00))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test @Order(92)
    void pagarReservaPagadaNuevamenteFalla() throws Exception {
        String body = String.format(
            "{\"bookingId\":%d,\"amount\":80.00,\"method\":\"YAPE\"}", createdBookingId);
        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }


    @Test @Order(100)
    void crearResenaRequiereReservaCompletada() throws Exception {
        String body = String.format(
            "{\"bookingId\":%d,\"rating\":5,\"comment\":\"Excelente\"}", createdBookingId);
        mockMvc.perform(post("/api/reviews")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(101)
    void crearResenaConReservaCompletada() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + createdBookingId + "/status")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isOk());

        String body = String.format(
            "{\"bookingId\":%d,\"rating\":5,\"comment\":\"Excelente trabajo, muy puntual.\"}", createdBookingId);
        mockMvc.perform(post("/api/reviews")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.comment").value("Excelente trabajo, muy puntual."));
    }

    @Test @Order(102)
    void verResenasDeProfesional() throws Exception {
        mockMvc.perform(get("/api/reviews/professional/" + createdProfessionalId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test @Order(103)
    void crearResenaDobleEnMismaReservaFalla() throws Exception {
        String body = String.format(
            "{\"bookingId\":%d,\"rating\":3,\"comment\":\"Duplicada\"}", createdBookingId);
        mockMvc.perform(post("/api/reviews")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(110)
    void verEstadoConfirmacion() throws Exception {
        mockMvc.perform(get("/api/confirmations/booking/" + createdBookingId)
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookingId").value(createdBookingId));
    }

    @Test @Order(111)
    void generarConfirmacion() throws Exception {
        mockMvc.perform(post("/api/confirmations/booking/" + createdBookingId + "/generate")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.confirmationCode").isNotEmpty());
    }

    @Test @Order(120)
    void actuatorHealthPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test @Order(130)
    void registroSinEmailFalla() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"password\":\"123456\",\"role\":\"CLIENT\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(131)
    void registroConEmailInvalidoFalla() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"no-es-email\",\"password\":\"123456\",\"role\":\"CLIENT\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(132)
    void registroConPasswordCortoFalla() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"test2@test.com\",\"password\":\"123\",\"role\":\"CLIENT\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @Order(133)
    void endpointInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
            .andExpect(status().is4xxClientError());
    }
}

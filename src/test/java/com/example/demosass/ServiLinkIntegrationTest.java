package com.example.demosass;

import com.example.demosass.domain.enums.Role;
import com.example.demosass.dto.request.AuthRequest.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ServiLinkIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String clientToken;
    private String professionalToken;

    @BeforeEach
    void setUp() throws Exception {
        // Registrar cliente
        RegisterRequest clientReq = new RegisterRequest(
            "Test Client", "client.test@servilink.pe", "password123", "999000001", Role.CLIENT
        );
        MvcResult clientResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientReq)))
            .andReturn();
        if (clientResult.getResponse().getStatus() == 201) {
            JsonNode node = objectMapper.readTree(clientResult.getResponse().getContentAsString());
            clientToken = node.get("token").asText();
        } else {
            // Ya existe, hacer login
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"client.test@servilink.pe\",\"password\":\"password123\"}"))
                .andReturn();
            JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            clientToken = node.get("token").asText();
        }

        // Registrar profesional
        RegisterRequest profReq = new RegisterRequest(
            "Test Professional", "prof.test@servilink.pe", "password123", "999000002", Role.PROFESSIONAL
        );
        MvcResult profResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profReq)))
            .andReturn();
        if (profResult.getResponse().getStatus() == 201) {
            JsonNode node = objectMapper.readTree(profResult.getResponse().getContentAsString());
            professionalToken = node.get("token").asText();
        } else {
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"prof.test@servilink.pe\",\"password\":\"password123\"}"))
                .andReturn();
            JsonNode node = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            professionalToken = node.get("token").asText();
        }
    }

    @Test
    void contextLoads() {}

    @Test
    void registerAndLoginFlow() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Test User", "test.unique@servilink.pe", "password123", "999999999", Role.CLIENT
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("CLIENT"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test.unique@servilink.pe\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void getCategoriesPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk());
    }

    @Test
    void nearbySearchPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/professionals/nearby")
                .param("lat", "-12.0464")
                .param("lon", "-77.0428")
                .param("radius", "10.0"))
            .andExpect(status().isOk());
    }

    @Test
    void mapGeoDistanceEndpoint() throws Exception {
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "-12.0464").param("lon1", "-77.0428")
                .param("lat2", "-12.0700").param("lon2", "-77.0500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").isNumber());
    }

    // ── Nuevos tests ──────────────────────────────────────────────────────────

    @Test
    void notificationsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getNotificationsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk());
    }

    @Test
    void getUnreadNotificationsCount() throws Exception {
        mockMvc.perform(get("/api/notifications/unread/count")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").isNumber());
    }

    @Test
    void markAllNotificationsAsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.markedAsRead").isNumber());
    }

    @Test
    void messagesRequireAuth() throws Exception {
        mockMvc.perform(get("/api/messages/booking/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void unreadMessagesCount() throws Exception {
        mockMvc.perform(get("/api/messages/unread/count")
                .header("Authorization", "Bearer " + clientToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unreadCount").isNumber());
    }

    @Test
    void availabilityPublicEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/availability/professional/1"))
            .andExpect(status().isOk());
    }

    @Test
    void createAvailabilityRequiresProfessional() throws Exception {
        mockMvc.perform(post("/api/availability")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"08:00\",\"endTime\":\"17:00\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void professionalCanCreateAvailability() throws Exception {
        // Primero crear perfil profesional
        mockMvc.perform(post("/api/professionals/profile")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"specialty\":\"Plomería\",\"baseRate\":45.00,\"address\":\"Lima\"}"))
            .andReturn();

        mockMvc.perform(post("/api/availability")
                .header("Authorization", "Bearer " + professionalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"08:00\",\"endTime\":\"17:00\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }
}

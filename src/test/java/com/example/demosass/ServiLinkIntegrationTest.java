package com.example.demosass;

import com.example.demosass.domain.enums.Role;
import com.example.demosass.dto.request.AuthRequest.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ServiLinkIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void registerAndLoginFlow() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "Test User", "test@servilink.pe", "password123", "999999999", Role.CLIENT
        );

        // Registro
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("CLIENT"));

        // Login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"test@servilink.pe","password":"password123"}
                """))
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
}

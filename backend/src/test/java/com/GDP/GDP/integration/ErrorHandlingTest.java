package com.GDP.GDP.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;

/**
 * Malformed request bodies and mistyped path variables must surface as a 400 with a
 * client-facing error code, not fall through to the generic 500 handler.
 */
@DisplayName("Gestion des erreurs — corps de requête invalide et paramètres mal typés")
@SpringBootTest
@AutoConfigureMockMvc
public class ErrorHandlingTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );
        User savedUser = userRepository.save(
            new User("userA", "userA@mail.com", "password", Role.ROLE_USER)
        );
        userDetails = new CustomUserDetails(savedUser);
    }

    @Test
    @DisplayName("400 — corps JSON malformé, pas 500")
    void malformedJsonBody_shouldReturn400_notInternalError() throws Exception {
        mockMvc.perform(
            post("/api/business")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not-valid-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST_BODY"));
    }

    @Test
    @DisplayName("400 — path variable non numérique, pas 500")
    void nonNumericPathVariable_shouldReturn400_notInternalError() throws Exception {
        mockMvc.perform(
            delete("/api/job-offers/{id}", "abc")
                .with(user(userDetails)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }
}

package com.GDP.GDP.integration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.JobOfferRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TC-001 — Créer un business et plusieurs offres
 *   Topic : Création et persistance
 *   Scénario : Un utilisateur authentifié crée un business puis lui assigne
 *              plusieurs offres d'emploi. On vérifie la persistance, la FK user,
 *              le rattachement des offres et la cohérence de la lecture ultérieure.
 *
 * TC-002 — Associer plusieurs contacts (Professional) à un même business
 *   Topic : Création et persistance
 *   Scénario : Un utilisateur authentifié crée un business puis lui associe
 *              plusieurs contacts. On vérifie persistance, association et
 *              lecture cohérente via GET /api/business.
 */
@DisplayName("TC-001 / TC-002 — Création et persistance (Business + JobOffers + Professionals)")
@SpringBootTest
@AutoConfigureMockMvc
public class CreationEtPersistanceTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private JobOfferRepository jobOfferRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private User savedUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );
        savedUser = userRepository.save(
            new User("userA", "userA@mail.com", "password", Role.ROLE_USER)
        );
        userDetails = new CustomUserDetails(savedUser);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crée un business via l'API et retourne son id.
     */
    private Long createBusiness(String name) throws Exception {
        BusinessRequest req = new BusinessRequest(name, name + "_desc", name + "_contact");
        MvcResult result = mockMvc.perform(
            post("/api/business")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }

    /**
     * Crée une offre d'emploi rattachée à un business via l'API et retourne son id.
     */
    private Long createJobOffer(Long businessId, String name) throws Exception {
        JobOfferRequest req = new JobOfferRequest(name, "https://link/" + name, 7);
        MvcResult result = mockMvc.perform(
            post("/api/job-offers/{businessId}", businessId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }


    // =========================================================================
    // TC-001 — Business + plusieurs JobOffers
    // =========================================================================
    @Nested
    @DisplayName("TC-001 — Créer un business et plusieurs offres")
    class TC001_BusinessWithMultipleJobOffers {

        @Test
        @DisplayName("Le business est persisté avec la FK user correcte")
        void tc001_businessShouldBePersisted_withCorrectUserFK() throws Exception {
            Long businessId = createBusiness("Acme");

            var businesses = businessRepository.findAll();
            assertThat(businesses).hasSize(1);
            assertThat(businesses.get(0).getUser().getId()).isEqualTo(savedUser.getId());
            assertThat(businesses.get(0).getId()).isEqualTo(businessId);
        }

        @Test
        @DisplayName("Toutes les offres sont rattachées au business après création")
        void tc001_allJobOffersShouldBeAttached_toTheBusiness() throws Exception {
            Long businessId = createBusiness("Acme");
            Long offer1Id = createJobOffer(businessId, "Dev Java");
            Long offer2Id = createJobOffer(businessId, "Dev React");
            Long offer3Id = createJobOffer(businessId, "DevOps");

            var offers = jobOfferRepository.findAll();
            assertThat(offers).hasSize(3);
            assertThat(offers).extracting(jo -> jo.getBusiness().getId())
                .containsOnly(businessId);
            assertThat(offers).extracting(jo -> jo.getId())
                .containsExactlyInAnyOrder(offer1Id, offer2Id, offer3Id);
        }

        @Test
        @DisplayName("GET /api/business retourne le business avec toutes ses offres imbriquées")
        void tc001_getBusinessShouldReturnAllJobOffers_nested() throws Exception {
            Long businessId = createBusiness("Acme");
            createJobOffer(businessId, "Dev Java");
            createJobOffer(businessId, "Dev React");

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(businessId))
                .andExpect(jsonPath("$[0].jobOffersList").isArray())
                .andExpect(jsonPath("$[0].jobOffersList.length()").value(2))
                .andExpect(jsonPath("$[0].jobOffersList[0].name").exists())
                .andExpect(jsonPath("$[0].jobOffersList[1].name").exists());
        }

        @Test
        @DisplayName("401 — Créer un business sans être authentifié")
        void tc001_createBusiness_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(
                post("/api/business")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new BusinessRequest("X", "desc", "contact"))))
                .andExpect(status().isUnauthorized());

            assertThat(businessRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("401 — Créer une offre sans être authentifié")
        void tc001_createJobOffer_shouldReturn401_whenNotAuthenticated() throws Exception {
            Long businessId = createBusiness("Acme");

            mockMvc.perform(
                post("/api/job-offers/{businessId}", businessId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("Dev Java", "https://link", 7))))
                .andExpect(status().isUnauthorized());

            assertThat(jobOfferRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("400 — Créer une offre avec un nom vide doit échouer")
        void tc001_createJobOffer_shouldReturn400_whenNameIsBlank() throws Exception {
            Long businessId = createBusiness("Acme");

            mockMvc.perform(
                post("/api/job-offers/{businessId}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("", "https://link", 7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

            assertThat(jobOfferRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("404 — Créer une offre sur un business inexistant")
        void tc001_createJobOffer_shouldReturn404_whenBusinessDoesNotExist() throws Exception {
            mockMvc.perform(
                post("/api/job-offers/{businessId}", 9999L)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("Dev Java", "https://link", 7))))
                .andExpect(status().isNotFound());

            assertThat(jobOfferRepository.findAll()).isEmpty();
        }
    }

}
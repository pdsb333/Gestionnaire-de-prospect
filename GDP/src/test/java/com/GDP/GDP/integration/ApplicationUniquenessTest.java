package com.GDP.GDP.integration;

import java.time.LocalDateTime;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.ApplicationRepository;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.JobOfferRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TC-003 — Unicité de la démarche de candidature
 *   Topic : Création et persistance
 *   Scénario : Un utilisateur authentifié peut assigner UNE démarche de candidature
 *              à une offre d'emploi. La création nominale doit réussir (200/201).
 *
 * TC-004 — Empêcher deux démarches sur une même offre
 *   Topic : Contraintes métier
 *   Scénario : Tenter de créer une seconde application sur la même offre doit
 *              retourner 409 sans persister de doublon.
 */
@DisplayName("TC-003 / TC-004 — Unicité des démarches de candidature (Application)")
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationUniquenessTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private JobOfferRepository jobOfferRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private User savedUser;
    private CustomUserDetails userDetails;
    private Long businessId;
    private Long jobOfferId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );
        savedUser = userRepository.save(
            new User("userA", "userA@mail.com", "password", Role.ROLE_USER)
        );
        userDetails = new CustomUserDetails(savedUser);

        // Préconditions communes : business + offre existants
        businessId = createBusiness("Acme");
        jobOfferId = createJobOffer(businessId, "Dev Java");
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

    private Long createJobOffer(Long busId, String name) throws Exception {
        JobOfferRequest req = new JobOfferRequest(name, "https://link/" + name, 7);
        MvcResult result = mockMvc.perform(
            post("/api/job-offers/{businessId}", busId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(req)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }

    private String ValidApplicationBodyRequest(){
        LocalDateTime now = LocalDateTime.now();

        String body = """
            {
                "initialApplicationDate":"%s",
                "dateRelaunch":"%s"
            }
            """.formatted(
                now.minusDays(1),
                now.plusDays(7)
            );
        return body;
    }
    private MvcResult postApplication(Long joId) throws Exception {
        return mockMvc.perform(
            post("/api/application/{jobOfferId}", joId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ValidApplicationBodyRequest()))
            .andReturn();
    }

    // =========================================================================
    // TC-003 — Création nominale d'une démarche de candidature
    // =========================================================================
    @Nested
    @DisplayName("TC-003 — Unicité de la démarche de candidature (cas nominal)")
    class TC003_CreateApplicationNominal {

        @Test
        @DisplayName("201 — Créer une démarche sur une offre valide")
        void tc003_createApplication_shouldReturn201_whenJobOfferExistsAndNoExistingApplication() throws Exception {
            MvcResult result = postApplication(jobOfferId);

            assertThat(result.getResponse().getStatus()).isIn(200, 201);

            var apps = applicationRepository.findAll();
            assertThat(apps).hasSize(1);
            assertThat(apps.get(0).getOffer().getId()).isEqualTo(jobOfferId);
        }

        @Test
        @DisplayName("La démarche créée est correctement rattachée à l'offre")
        void tc003_createdApplication_shouldBeLinked_toCorrectJobOffer() throws Exception {
            MvcResult result = postApplication(jobOfferId);
            assertThat(result.getResponse().getStatus()).isIn(200, 201);

            var apps = applicationRepository.findAll();
            assertThat(apps).hasSize(1);
            assertThat(apps.get(0).getOffer().getId()).isEqualTo(jobOfferId);
        }

        @Test
        @DisplayName("401 — Créer une démarche sans être authentifié")
        void tc003_createApplication_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isUnauthorized());

            assertThat(applicationRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("404 — Créer une démarche sur une offre inexistante")
        void tc003_createApplication_shouldReturn404_whenJobOfferDoesNotExist() throws Exception {
            mockMvc.perform(
                post("/api/application/{jobOfferId}", 9999L)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isNotFound());

            assertThat(applicationRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Deux offres différentes peuvent chacune avoir leur démarche")
        void tc003_twoDistinctJobOffers_shouldEachHaveTheirOwnApplication() throws Exception {
            Long jobOffer2Id = createJobOffer(businessId, "Dev React");

            MvcResult r1 = postApplication(jobOfferId);
            MvcResult r2 = postApplication(jobOffer2Id);

            assertThat(r1.getResponse().getStatus()).isIn(200, 201);
            assertThat(r2.getResponse().getStatus()).isIn(200, 201);

            assertThat(applicationRepository.findAll()).hasSize(2);
        }
    }

    // =========================================================================
    // TC-004 — Contrainte d'unicité : une seule démarche par offre
    // =========================================================================
    @Nested
    @DisplayName("TC-004 — Empêcher deux démarches sur une même offre")
    class TC004_PreventDuplicateApplication {

        @Test
        @DisplayName("409 — Créer une seconde démarche sur la même offre doit échouer")
        void tc004_createApplication_shouldReturn409_whenApplicationAlreadyExists() throws Exception {
            // Première démarche → OK
            MvcResult first = postApplication(jobOfferId);
            assertThat(first.getResponse().getStatus()).isIn(200, 201);

            // Deuxième tentative → 409
            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Aucun doublon persisté après tentative de seconde création")
        void tc004_noDuplicate_shouldBePersisted_afterConflict() throws Exception {
            postApplication(jobOfferId);

            // Tentative de doublon
            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isConflict());

            // Exactement 1 seule application en base
            assertThat(applicationRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("409 — Le code d'erreur est explicite sur la contrainte violée")
        void tc004_conflict_shouldReturnExplicitErrorCode() throws Exception {
            postApplication(jobOfferId);

            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/application/" + jobOfferId));
        }

        @Test
        @DisplayName("Idempotence : une offre différente reste non affectée par le conflit")
        void tc004_conflict_shouldNotAffectOtherJobOffers() throws Exception {
            Long jobOffer2Id = createJobOffer(businessId, "Dev React");

            // Conflit sur offre 1
            postApplication(jobOfferId);
            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ValidApplicationBodyRequest()))
                .andExpect(status().isConflict());

            // L'offre 2 peut encore être associée
            MvcResult r2 = postApplication(jobOffer2Id);
            assertThat(r2.getResponse().getStatus()).isIn(200, 201);
            assertThat(applicationRepository.findAll()).hasSize(2);
        }
    }
}
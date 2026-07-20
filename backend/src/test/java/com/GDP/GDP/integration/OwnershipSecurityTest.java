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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.professional.ProfessionalRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.ApplicationRepository;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.JobOfferRepository;
import com.GDP.GDP.repository.ProfessionalRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TC-006 — Un user ne voit pas les données d'un autre
 *   Topic : Sécurité et isolation
 *   Scénario : User B authentifié tente de lire les données créées par User A.
 *              Le filtrage par ownership doit garantir une 404 (ressource invisible).
 *
 * TC-007 — Un user ne modifie/supprime pas les données d'un autre
 *   Topic : Sécurité et isolation
 *   Scénario : User B authentifié tente de modifier ou supprimer les ressources
 *              de User A. Chaque tentative doit échouer en 404, les données
 *              de User A restant intactes.
 *
 *   Ces tests valident :
 *     - Sécurité métier (filtrage ownership)
 *     - Contrôle d'accès au niveau des ressources
 *     - Aucune fuite de données entre utilisateurs
 */
@DisplayName("TC-006 / TC-007 — Sécurité et isolation inter-utilisateurs")
@SpringBootTest
@AutoConfigureMockMvc
public class OwnershipSecurityTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private JobOfferRepository jobOfferRepository;
    @Autowired private ProfessionalRepository professionalRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private User userA;
    private User userB;
    private CustomUserDetails userADetails;
    private CustomUserDetails userBDetails;

    // Ressources appartenant à User A
    private Long businessAId;
    private Long jobOfferAId;
    private Long professionalAId;
    private Long applicationAId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );

        // Créer user A et user B
        userA = userRepository.save(new User("userA", "userA@mail.com", "password", Role.ROLE_USER));
        userB = userRepository.save(new User("userB", "userB@mail.com", "password", Role.ROLE_USER));
        userADetails = new CustomUserDetails(userA);
        userBDetails = new CustomUserDetails(userB);

        // Construire le graphe complet appartenant à User A
        businessAId    = createBusiness("Acme", userADetails);
        jobOfferAId    = createJobOffer(businessAId, "Dev Java", userADetails);
        professionalAId = createProfessional(businessAId, "Dupont", "Alice", userADetails);
        createApplication(jobOfferAId, userADetails);

        // Récupérer l'id de l'application créée
        var apps = applicationRepository.findAll();
        assertThat(apps).hasSize(1);
        applicationAId = apps.get(0).getId();
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

    private Long createBusiness(String name, CustomUserDetails as) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/business")
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new BusinessRequest(name, name + "_desc", name + "_contact"))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createJobOffer(Long businessId, String name, CustomUserDetails as) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/job-offers/{businessId}", businessId)
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new JobOfferRequest(name, "https://link/" + name, 7))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createProfessional(Long businessId, String lastName, String firstName, CustomUserDetails as) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/professionals/{businessId}", businessId)
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new ProfessionalRequest(lastName, firstName, "Recruiter", "mail@test.com"))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createApplication(Long jobOfferId, CustomUserDetails as) throws Exception {
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
        mockMvc.perform(
            post("/api/application/{jobOfferId}", jobOfferId)
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
    }

    // =========================================================================
    // TC-006 — User B ne voit pas les données de User A (lecture)
    // =========================================================================
    @Nested
    @DisplayName("TC-006 — Isolation en lecture : User B ne voit pas les données de User A")
    class TC006_ReadIsolation {

        @Test
        @DisplayName("GET /api/business — User B ne voit aucun business de User A")
        void tc006_userB_shouldNotSee_userA_businesses_viaGetAll() throws Exception {
            mockMvc.perform(
                get("/api/business")
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("GET /api/business — User A voit uniquement ses propres businesses")
        void tc006_userA_shouldOnlySee_ownBusinesses_notUserB() throws Exception {
            // User B crée son propre business
            createBusiness("Beta Corp", userBDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userADetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Acme"));
        }

        @Test
        @DisplayName("PUT /api/business/{id} — User B tente de lire/accéder au business de User A → 404")
        void tc006_userB_shouldGet404_whenAccessingUserA_business_viaUpdate() throws Exception {
            // Un PUT avec User B sur le business de A → 404 (ressource invisible)
            mockMvc.perform(
                put("/api/business/{id}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new BusinessRequest("Spy", "desc", "contact"))))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/job-offers/{id} — User B tente d'accéder à l'offre de User A → 404")
        void tc006_userB_shouldGet404_whenAccessingUserA_jobOffer_viaUpdate() throws Exception {
            mockMvc.perform(
                put("/api/job-offers/{id}", jobOfferAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("Spy Offer", "https://spy", 1))))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/professionals/{id} — User B tente d'accéder au contact de User A → 404")
        void tc006_userB_shouldGet404_whenAccessingUserA_professional_viaUpdate() throws Exception {
            mockMvc.perform(
                put("/api/professionals/{id}", professionalAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new ProfessionalRequest("Spy", "Spy", "Spy", "spy@mail.com"))))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Les données de User A restent intactes après les tentatives de User B")
        void tc006_userA_data_shouldRemainIntact_afterUserB_readAttempts() throws Exception {
            // User B tente de lire en passant par update
            mockMvc.perform(
                put("/api/business/{id}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new BusinessRequest("Spy", "desc", "contact"))))
                .andExpect(status().isNotFound());

            // Vérifier que les données de A sont intactes
            var business = businessRepository.findById(businessAId).orElseThrow();
            assertThat(business.getName()).isEqualTo("Acme");
            assertThat(business.getUser().getId()).isEqualTo(userA.getId());
        }
    }

    // =========================================================================
    // TC-007 — User B ne modifie/supprime pas les données de User A
    // =========================================================================
    @Nested
    @DisplayName("TC-007 — Isolation en écriture : User B ne peut pas modifier/supprimer les données de User A")
    class TC007_WriteIsolation {

        // ── Modification ─────────────────────────────────────────────────────

        @Test
        @DisplayName("PUT /api/business/{id} — User B ne peut pas modifier le business de User A")
        void tc007_userB_shouldNotModify_userA_business() throws Exception {
            mockMvc.perform(
                put("/api/business/{id}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new BusinessRequest("Modifié par B", "desc", "contact"))))
                .andExpect(status().isNotFound());

            var unchanged = businessRepository.findById(businessAId).orElseThrow();
            assertThat(unchanged.getName()).isEqualTo("Acme");
        }

        @Test
        @DisplayName("PUT /api/job-offers/{id} — User B ne peut pas modifier l'offre de User A")
        void tc007_userB_shouldNotModify_userA_jobOffer() throws Exception {
            mockMvc.perform(
                put("/api/job-offers/{id}", jobOfferAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("Offre modifiée", "https://spy", 1))))
                .andExpect(status().isNotFound());

            var unchanged = jobOfferRepository.findById(jobOfferAId).orElseThrow();
            assertThat(unchanged.getName()).isEqualTo("Dev Java");
        }

        @Test
        @DisplayName("PUT /api/professionals/{id} — User B ne peut pas modifier le contact de User A")
        void tc007_userB_shouldNotModify_userA_professional() throws Exception {
            mockMvc.perform(
                put("/api/professionals/{id}", professionalAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new ProfessionalRequest("Modifié", "Par B", "Spy", "spy@mail.com"))))
                .andExpect(status().isNotFound());

            var unchanged = professionalRepository.findById(professionalAId).orElseThrow();
            assertThat(unchanged.getLastName()).isEqualTo("Dupont");
        }

        // ── Suppression ──────────────────────────────────────────────────────

        @Test
        @DisplayName("DELETE /api/business/{id} — User B ne peut pas supprimer le business de User A")
        void tc007_userB_shouldNotDelete_userA_business() throws Exception {
            mockMvc.perform(
                delete("/api/business/{id}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            assertThat(businessRepository.findById(businessAId)).isPresent();
        }

        @Test
        @DisplayName("DELETE /api/job-offers/{id} — User B ne peut pas supprimer l'offre de User A")
        void tc007_userB_shouldNotDelete_userA_jobOffer() throws Exception {
            mockMvc.perform(
                delete("/api/job-offers/{id}", jobOfferAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            assertThat(jobOfferRepository.findById(jobOfferAId)).isPresent();
        }

        @Test
        @DisplayName("DELETE /api/professionals/{id} — User B ne peut pas supprimer le contact de User A")
        void tc007_userB_shouldNotDelete_userA_professional() throws Exception {
            mockMvc.perform(
                delete("/api/professionals/{id}", professionalAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            assertThat(professionalRepository.findById(professionalAId)).isPresent();
        }

        @Test
        @DisplayName("DELETE /api/application/{id} — User B ne peut pas supprimer la démarche de User A")
        void tc007_userB_shouldNotDelete_userA_application() throws Exception {
            mockMvc.perform(
                delete("/api/application/{applicationId}", applicationAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            assertThat(applicationRepository.findById(applicationAId)).isPresent();
        }

        @Test
        @DisplayName("PUT /api/application/{id} — User B ne peut pas modifier la démarche de User A")
        void tc007_userB_shouldNotModify_userA_application() throws Exception {
            var before = applicationRepository.findById(applicationAId).orElseThrow();
            var originalInitialDate = before.getInitialApplicationDate();

            String body = """
                {
                    "initialApplicationDate":"%s"
                }
                """.formatted(originalInitialDate.minusDays(30));

            mockMvc.perform(
                put("/api/application/{applicationId}", applicationAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isNotFound());

            var unchanged = applicationRepository.findById(applicationAId).orElseThrow();
            assertThat(unchanged.getInitialApplicationDate()).isEqualTo(originalInitialDate);
        }

        @Test
        @DisplayName("POST /api/application/{id}/relance — User B ne peut pas relancer la démarche de User A")
        void tc007_userB_shouldNotRelaunch_userA_application() throws Exception {
            var originalDateRelaunch = applicationRepository.findById(applicationAId).orElseThrow().getDateRelaunch();
            Integer originalHistorySize = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM application_history_of_relaunches WHERE application_id = ?",
                Integer.class, applicationAId);

            mockMvc.perform(
                post("/api/application/{applicationId}/relance", applicationAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

            var unchanged = applicationRepository.findById(applicationAId).orElseThrow();
            assertThat(unchanged.getDateRelaunch()).isEqualTo(originalDateRelaunch);
            Integer unchangedHistorySize = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM application_history_of_relaunches WHERE application_id = ?",
                Integer.class, applicationAId);
            assertThat(unchangedHistorySize).isEqualTo(originalHistorySize);
        }

        // ── Vérification globale post-attaque ─────────────────────────────────

        @Test
        @DisplayName("L'ensemble du graphe de User A reste intact après toutes les tentatives de User B")
        void tc007_fullGraphOfUserA_shouldRemainIntact_afterAllUserBAttempts() throws Exception {
            // Tentatives de mutation de User B
            mockMvc.perform(put("/api/business/{id}", businessAId).with(user(userBDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new BusinessRequest("Spy", "d", "c")))).andReturn();
            mockMvc.perform(delete("/api/business/{id}", businessAId).with(user(userBDetails))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
            mockMvc.perform(put("/api/job-offers/{id}", jobOfferAId).with(user(userBDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new JobOfferRequest("Spy", "https://s", 1)))).andReturn();
            mockMvc.perform(delete("/api/job-offers/{id}", jobOfferAId).with(user(userBDetails))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

            // Vérification d'intégrité complète
            assertThat(businessRepository.findById(businessAId)).isPresent();
            assertThat(jobOfferRepository.findById(jobOfferAId)).isPresent();
            assertThat(professionalRepository.findById(professionalAId)).isPresent();
            assertThat(applicationRepository.findById(applicationAId)).isPresent();

            var business = businessRepository.findById(businessAId).orElseThrow();
            assertThat(business.getName()).isEqualTo("Acme");
            assertThat(business.getUser().getId()).isEqualTo(userA.getId());
        }

        // ── Cas non authentifié ───────────────────────────────────────────────

        @Test
        @DisplayName("DELETE /api/business/{id} — 401 sans authentification")
        void tc007_deleteWithoutAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                delete("/api/business/{id}", businessAId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

            assertThat(businessRepository.findById(businessAId)).isPresent();
        }
    }

    // =========================================================================
    // TC-008 — User B ne peut pas créer de ressource sous une entité de User A
    // =========================================================================
    @Nested
    @DisplayName("TC-008 — Isolation en création : User B ne peut pas créer sous les entités de User A")
    class TC008_CreateIsolation {

        @Test
        @DisplayName("POST /api/job-offers/{businessId} — User B ne peut pas créer d'offre sous le business de User A")
        void tc008_userB_shouldNotCreate_jobOffer_underUserA_business() throws Exception {
            mockMvc.perform(
                post("/api/job-offers/{businessId}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new JobOfferRequest("Spy Offer", "https://spy", 1))))
                .andExpect(status().isNotFound());

            assertThat(jobOfferRepository.findAll()).hasSize(1); // seulement celle de User A créée en setUp
        }

        @Test
        @DisplayName("POST /api/professionals/{businessId} — User B ne peut pas créer de contact sous le business de User A")
        void tc008_userB_shouldNotCreate_professional_underUserA_business() throws Exception {
            mockMvc.perform(
                post("/api/professionals/{businessId}", businessAId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new ProfessionalRequest("Spy", "Spy", "Spy", "spy@mail.com"))))
                .andExpect(status().isNotFound());

            assertThat(professionalRepository.findAll()).hasSize(1); // seulement celui de User A créé en setUp
        }

        @Test
        @DisplayName("POST /api/application/{jobOfferId} — User B ne peut pas créer de démarche sous l'offre de User A")
        void tc008_userB_shouldNotCreate_application_underUserA_jobOffer() throws Exception {
            // Nouvelle offre de User A sans démarche, pour isoler ce test sur la vérification
            // d'ownership plutôt que sur la détection de doublon (jobOfferAId en a déjà une,
            // créée en setUp).
            Long jobOfferWithoutApplicationId = createJobOffer(businessAId, "Dev Python", userADetails);

            String body = """
                {
                    "initialApplicationDate":"%s"
                }
                """.formatted(LocalDateTime.now().minusDays(1));

            mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferWithoutApplicationId)
                    .with(user(userBDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isNotFound());

            assertThat(applicationRepository.findAll()).hasSize(1); // uniquement celle de User A créée en setUp
        }
    }
}
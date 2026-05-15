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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * TC-008 — Supprimer un business supprime tout le graphe (cascade delete)
 *   Topic : Cohérence relationnelle
 *   Scénario : Un utilisateur supprime un business qui possède des contacts,
 *              des offres d'emploi et des démarches de candidature.
 *              La suppression doit être en cascade : plus aucune entité liée
 *              ne doit subsister en base.
 *
 *   Ce test valide :
 *     - CASCADE DELETE réelle sur toutes les entités dépendantes
 *     - Absence d'entités orphelines post-suppression
 *     - Intégrité référentielle (pas de FK pendantes)
 *     - Les données d'un autre business / user ne sont pas affectées
 */
@DisplayName("TC-008 — Suppression en cascade du graphe business")
@SpringBootTest
@AutoConfigureMockMvc
public class CascadeDeleteTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private JobOfferRepository jobOfferRepository;
    @Autowired private ProfessionalRepository professionalRepository;
    @Autowired private ApplicationRepository applicationRepository;
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

    private Long createBusiness(String name) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/business")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new BusinessRequest(name, name + "_desc", name + "_contact"))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createJobOffer(Long businessId, String name) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/job-offers/{businessId}", businessId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new JobOfferRequest(name, "https://link/" + name, 7))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createProfessional(Long businessId, String lastName) throws Exception {
        MvcResult r = mockMvc.perform(
            post("/api/professionals/{businessId}", businessId)
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new ProfessionalRequest(lastName, "Prenom", "Recruiter", "mail@test.com"))))
            .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createApplication(Long jobOfferId) throws Exception {
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
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        return applicationRepository.findAll().stream()
            .filter(a -> a.getOffer().getId().equals(jobOfferId))
            .findFirst()
            .orElseThrow()
            .getId();
    }

    // =========================================================================
    // Graphe minimal (business seul)
    // =========================================================================
    @Nested
    @DisplayName("Suppression d'un business sans enfants")
    class DeleteEmptyBusiness {

        @Test
        @DisplayName("DELETE retourne 204 et le business n'existe plus en base")
        void tc008_deleteEmptyBusiness_shouldReturn204_andRemoveFromDB() throws Exception {
            Long businessId = createBusiness("Acme");

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            assertThat(businessRepository.findById(businessId)).isEmpty();
        }

        @Test
        @DisplayName("DELETE d'un business inexistant retourne 404")
        void tc008_deleteNonExistentBusiness_shouldReturn404() throws Exception {
            mockMvc.perform(
                delete("/api/business/{id}", 9999L)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 — DELETE sans authentification")
        void tc008_deleteWithoutAuth_shouldReturn401() throws Exception {
            Long businessId = createBusiness("Acme");

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

            assertThat(businessRepository.findById(businessId)).isPresent();
        }
    }

    // =========================================================================
    // Cascade sur les offres
    // =========================================================================
    @Nested
    @DisplayName("Cascade sur les offres d'emploi")
    class CascadeOnJobOffers {

        @Test
        @DisplayName("Supprimer le business supprime toutes ses offres")
        void tc008_deleteBusinessShouldCascade_toJobOffers() throws Exception {
            Long businessId = createBusiness("Acme");
            Long offer1 = createJobOffer(businessId, "Dev Java");
            Long offer2 = createJobOffer(businessId, "Dev React");
            Long offer3 = createJobOffer(businessId, "DevOps");

            assertThat(jobOfferRepository.findAll()).hasSize(3);

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            assertThat(jobOfferRepository.findById(offer1)).isEmpty();
            assertThat(jobOfferRepository.findById(offer2)).isEmpty();
            assertThat(jobOfferRepository.findById(offer3)).isEmpty();
            assertThat(jobOfferRepository.findAll()).isEmpty();
        }
    }

    // =========================================================================
    // Cascade sur les contacts
    // =========================================================================
    @Nested
    @DisplayName("Cascade sur les contacts (Professionals)")
    class CascadeOnProfessionals {

        @Test
        @DisplayName("Supprimer le business supprime tous ses contacts")
        void tc008_deleteBusinessShouldCascade_toProfessionals() throws Exception {
            Long businessId = createBusiness("Acme");
            Long pro1 = createProfessional(businessId, "Dupont");
            Long pro2 = createProfessional(businessId, "Martin");

            assertThat(professionalRepository.findAll()).hasSize(2);

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            assertThat(professionalRepository.findById(pro1)).isEmpty();
            assertThat(professionalRepository.findById(pro2)).isEmpty();
            assertThat(professionalRepository.findAll()).isEmpty();
        }
    }

    // =========================================================================
    // Cascade sur les démarches de candidature (via offres)
    // =========================================================================
    @Nested
    @DisplayName("Cascade sur les démarches de candidature (Applications)")
    class CascadeOnApplications {

        @Test
        @DisplayName("Supprimer le business supprime les démarches associées aux offres")
        void tc008_deleteBusinessShouldCascade_toApplicationsViaJobOffers() throws Exception {
            Long businessId = createBusiness("Acme");
            Long offer1 = createJobOffer(businessId, "Dev Java");
            Long offer2 = createJobOffer(businessId, "Dev React");
            Long app1 = createApplication(offer1);
            Long app2 = createApplication(offer2);

            assertThat(applicationRepository.findAll()).hasSize(2);

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            assertThat(applicationRepository.findById(app1)).isEmpty();
            assertThat(applicationRepository.findById(app2)).isEmpty();
            assertThat(applicationRepository.findAll()).isEmpty();
        }
    }

    // =========================================================================
    // Graphe complet — le test principal de TC-008
    // =========================================================================
    @Nested
    @DisplayName("Cascade complète sur le graphe entier")
    class CascadeFullGraph {

        @Test
        @DisplayName("DELETE business : plus aucune entité liée en base (graphe complet)")
        void tc008_deleteFullGraphBusiness_shouldLeaveNoOrphanEntities() throws Exception {
            // ── ARRANGE : construire le graphe complet ──
            Long businessId = createBusiness("Acme");

            Long offer1 = createJobOffer(businessId, "Dev Java");
            Long offer2 = createJobOffer(businessId, "Dev React");
            Long offer3 = createJobOffer(businessId, "DevOps");

            Long pro1 = createProfessional(businessId, "Dupont");
            Long pro2 = createProfessional(businessId, "Martin");

            Long app1 = createApplication(offer1);
            Long app2 = createApplication(offer2);
            // offer3 n'a pas de démarche → vérifie qu'elle est bien supprimée aussi

            // Vérification du dataset initial
            assertThat(businessRepository.findAll()).hasSize(1);
            assertThat(jobOfferRepository.findAll()).hasSize(3);
            assertThat(professionalRepository.findAll()).hasSize(2);
            assertThat(applicationRepository.findAll()).hasSize(2);

            // ── ACT ──
            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            // ── ASSERT : tout le graphe est supprimé ──
            assertThat(businessRepository.findById(businessId)).isEmpty();

            assertThat(jobOfferRepository.findById(offer1)).isEmpty();
            assertThat(jobOfferRepository.findById(offer2)).isEmpty();
            assertThat(jobOfferRepository.findById(offer3)).isEmpty();
            assertThat(jobOfferRepository.findAll()).isEmpty();

            assertThat(professionalRepository.findById(pro1)).isEmpty();
            assertThat(professionalRepository.findById(pro2)).isEmpty();
            assertThat(professionalRepository.findAll()).isEmpty();

            assertThat(applicationRepository.findById(app1)).isEmpty();
            assertThat(applicationRepository.findById(app2)).isEmpty();
            assertThat(applicationRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("La suppression d'un business n'affecte pas les données d'un autre business")
        void tc008_deleteOneBusiness_shouldNotAffect_otherBusinessGraphs() throws Exception {
            // ── ARRANGE ──
            Long bizA = createBusiness("Acme");
            Long offerA = createJobOffer(bizA, "Dev Java A");
            Long proA   = createProfessional(bizA, "DupontA");
            Long appA   = createApplication(offerA);

            Long bizB = createBusiness("Beta Corp");
            Long offerB = createJobOffer(bizB, "Dev Java B");
            Long proB   = createProfessional(bizB, "DupontB");
            Long appB   = createApplication(offerB);

            // ── ACT : supprimer seulement A ──
            mockMvc.perform(
                delete("/api/business/{id}", bizA)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            // ── ASSERT : A est supprimé ──
            assertThat(businessRepository.findById(bizA)).isEmpty();
            assertThat(jobOfferRepository.findById(offerA)).isEmpty();
            assertThat(professionalRepository.findById(proA)).isEmpty();
            assertThat(applicationRepository.findById(appA)).isEmpty();

            // ── ASSERT : B est intact ──
            assertThat(businessRepository.findById(bizB)).isPresent();
            assertThat(jobOfferRepository.findById(offerB)).isPresent();
            assertThat(professionalRepository.findById(proB)).isPresent();
            assertThat(applicationRepository.findById(appB)).isPresent();

            var businessB = businessRepository.findById(bizB).orElseThrow();
            assertThat(businessB.getName()).isEqualTo("Beta Corp");
        }

        @Test
        @DisplayName("L'utilisateur (User) n'est pas supprimé lors de la suppression du business")
        void tc008_deleteBusinessShouldNotDelete_theOwnerUser() throws Exception {
            Long businessId = createBusiness("Acme");

            mockMvc.perform(
                delete("/api/business/{id}", businessId)
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            assertThat(userRepository.findById(savedUser.getId())).isPresent();
        }
    }
}
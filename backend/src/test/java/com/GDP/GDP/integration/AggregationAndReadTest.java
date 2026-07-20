package com.GDP.GDP.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import com.GDP.GDP.dto.professional.ProfessionalRequest;
import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TC-005 — Charger business + offres + démarches + contacts
 *   Topic : Agrégation et lecture
 *   Scénario : À partir d'un dataset relationnel complet (business → offres → démarches,
 *              business → contacts), GET /api/business doit retourner une structure
 *              imbriquée complète sans données orphelines, en respectant le contrat
 *              du DTO BusinessResponse.
 *
 *   Ce test valide :
 *     - Mappings imbriqués (business → jobOffersList → application, professionalsList)
 *     - Fetch strategy (pas de lazy loading foireux / N+1)
 *     - Sérialisation JSON conforme au contrat API
 *     - Cohérence des DTOs (aucun champ null inattendu, aucune donnée orpheline)
 *     - Isolation par user (un user ne voit que ses propres businesses)
 */
@DisplayName("TC-005 — Agrégation et lecture du graphe complet (GET /api/business)")
@SpringBootTest
@AutoConfigureMockMvc
public class AggregationAndReadTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BusinessRepository businessRepository;
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
    // Helpers : construction du dataset relationnel via l'API
    // ─────────────────────────────────────────────────────────────────────────

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Long createBusiness(String name, CustomUserDetails as) throws Exception {
        MvcResult result = mockMvc.perform(
            post("/api/business")
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new BusinessRequest(name, name + "_desc", name + "_contact"))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createJobOffer(Long businessId, String name, CustomUserDetails as) throws Exception {
        MvcResult result = mockMvc.perform(
            post("/api/job-offers/{businessId}", businessId)
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new JobOfferRequest(name, "https://link/" + name, 7))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createProfessional(Long businessId, String lastName, CustomUserDetails as) throws Exception {
        MvcResult result = mockMvc.perform(
            post("/api/professionals/{businessId}", businessId)
                .with(user(as))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new ProfessionalRequest(lastName, "Prenom", "Recruiter", "mail@test.com"))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
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
    // Tests de lecture / agrégation
    // =========================================================================

    @Nested
    @DisplayName("Structure de la réponse")
    class ResponseStructure {

        @Test
        @DisplayName("GET retourne 200 avec un tableau de businesses pour l'utilisateur connecté")
        void tc005_getBusiness_shouldReturn200_withBusinessArray() throws Exception {
            createBusiness("Acme", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Chaque business contient les champs du contrat DTO")
        void tc005_businessResponse_shouldContainAllContractFields() throws Exception {
            createBusiness("Acme", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").value("Acme"))
                .andExpect(jsonPath("$[0].description").value("Acme_desc"))
                .andExpect(jsonPath("$[0].recruitmentServiceContact").value("Acme_contact"))
                .andExpect(jsonPath("$[0].jobOffersList").isArray())
                .andExpect(jsonPath("$[0].professionalsList").isArray());
        }

        @Test
        @DisplayName("Un business sans offres ni contacts retourne des listes vides (pas null)")
        void tc005_businessWithNoChildren_shouldReturnEmptyLists_notNull() throws Exception {
            createBusiness("Acme", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobOffersList").isArray())
                .andExpect(jsonPath("$[0].jobOffersList.length()").value(0))
                .andExpect(jsonPath("$[0].professionalsList").isArray())
                .andExpect(jsonPath("$[0].professionalsList.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Mappings imbriqués — offres et démarches")
    class NestedJobOffersAndApplications {

        @Test
        @DisplayName("Les offres sont correctement imbriquées dans le business")
        void tc005_jobOffersShouldBeNested_insideBusiness() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            createJobOffer(businessId, "Dev Java", userDetails);
            createJobOffer(businessId, "Dev React", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobOffersList.length()").value(2))
                .andExpect(jsonPath("$[0].jobOffersList[0].id").isNumber())
                .andExpect(jsonPath("$[0].jobOffersList[0].name").isString())
                .andExpect(jsonPath("$[0].jobOffersList[0].link").isString())
                .andExpect(jsonPath("$[0].jobOffersList[0].relaunchFrequency").isNumber());
        }

        @Test
        @DisplayName("Chaque offre avec démarche contient un objet application imbriqué")
        void tc005_jobOfferWithApplication_shouldContainNestedApplicationObject() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            Long jobOfferId = createJobOffer(businessId, "Dev Java", userDetails);
            createApplication(jobOfferId, userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobOffersList[0].application").exists())
                .andExpect(jsonPath("$[0].jobOffersList[0].application").isNotEmpty())
                .andReturn();
        }

        @Test
        @DisplayName("Une offre sans démarche a application null ou absent")
        void tc005_jobOfferWithoutApplication_shouldHaveNullOrAbsentApplication() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            createJobOffer(businessId, "Dev Java", userDetails);

            MvcResult result = mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

            // Soit null, soit absent — on vérifie qu'aucune application fantôme n'apparaît
            String body = result.getResponse().getContentAsString();
            var tree = objectMapper.readTree(body);
            var applicationNode = tree.get(0).get("jobOffersList").get(0).get("application");
            assertThat(applicationNode == null || applicationNode.isNull()).isTrue();
        }

        @Test
        @DisplayName("Le graphe complet : business + 2 offres (1 avec démarche) + 2 contacts")
        void tc005_fullGraph_shouldBeReturned_withExactAssociations() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            Long offer1Id   = createJobOffer(businessId, "Dev Java", userDetails);
            Long offer2Id   = createJobOffer(businessId, "Dev React", userDetails);
            createApplication(offer1Id, userDetails);
            createProfessional(businessId, "Dupont", userDetails);
            createProfessional(businessId, "Martin", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobOffersList.length()").value(2))
                .andExpect(jsonPath("$[0].professionalsList.length()").value(2));
        }
    }

    @Nested
    @DisplayName("Historique des relances — ordre chronologique")
    class HistoryOfRelaunchesOrdering {

        private Long createApplicationAndGetId(Long jobOfferId, CustomUserDetails as) throws Exception {
            String body = """
                {
                    "initialApplicationDate":"%s"
                }
                """.formatted(LocalDateTime.now().minusDays(30));

            MvcResult result = mockMvc.perform(
                post("/api/application/{jobOfferId}", jobOfferId)
                    .with(user(as))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isCreated())
                .andReturn();
            return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        }

        @Test
        @DisplayName("historyOfRelaunches est retourné en ordre chronologique croissant après plusieurs relances")
        void tc005_historyOfRelaunches_shouldBeReturnedInChronologicalOrder() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            Long jobOfferId = createJobOffer(businessId, "Dev Java", userDetails);
            Long applicationId = createApplicationAndGetId(jobOfferId, userDetails);

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(
                    post("/api/application/{applicationId}/relance", applicationId)
                        .with(user(userDetails)))
                    .andExpect(status().isOk());
                Thread.sleep(5); // garantit des timestamps distincts (troncature à la microseconde)
            }

            MvcResult result = mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

            var history = objectMapper.readTree(result.getResponse().getContentAsString())
                .get(0).get("jobOffersList").get(0).get("application").get("historyOfRelaunches");

            assertThat(history.isArray()).isTrue();
            assertThat(history.size()).isEqualTo(4); // date initiale + 3 relances

            List<LocalDateTime> dates = new ArrayList<>();
            history.forEach(node -> dates.add(LocalDateTime.parse(node.asText())));

            assertThat(dates).isSorted();
        }
    }

    @Nested
    @DisplayName("Mappings imbriqués — contacts professionnels")
    class NestedProfessionals {

        @Test
        @DisplayName("Les contacts sont correctement imbriqués dans le business")
        void tc005_professionalsShouldBeNested_insideBusiness() throws Exception {
            Long businessId = createBusiness("Acme", userDetails);
            createProfessional(businessId, "Dupont", userDetails);
            createProfessional(businessId, "Martin", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professionalsList.length()").value(2))
                .andExpect(jsonPath("$[0].professionalsList[0].id").isNumber())
                .andExpect(jsonPath("$[0].professionalsList[0].lastName").isString())
                .andExpect(jsonPath("$[0].professionalsList[0].firstName").isString());
        }
    }

    @Nested
    @DisplayName("Cohérence des données — absence d'orphelins et isolation")
    class DataCoherenceAndIsolation {

        @Test
        @DisplayName("Plusieurs businesses du même user sont tous retournés")
        void tc005_multipleBusinesses_shouldAllBeReturned_forSameUser() throws Exception {
            createBusiness("Acme", userDetails);
            createBusiness("Beta Corp", userDetails);
            createBusiness("Gamma Inc", userDetails);

            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("Les données d'un autre user ne polluent pas la réponse")
        void tc005_otherUserData_shouldNotAppear_inCurrentUserResponse() throws Exception {
            // User A crée 1 business
            createBusiness("Acme", userDetails);

            // User B crée ses propres données
            User userB = userRepository.save(new User("userB", "userB@mail.com", "pass", Role.ROLE_USER));
            CustomUserDetails userBDetails = new CustomUserDetails(userB);
            Long bizB = createBusiness("Beta Corp", userBDetails);
            createJobOffer(bizB, "Dev Java", userBDetails);

            // User A ne voit que son propre business
            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Acme"));
        }

        @Test
        @DisplayName("Les offres du business B ne remontent pas dans le business A")
        void tc005_jobOffersFromBusinessB_shouldNotAppear_inBusinessA() throws Exception {
            Long bizA = createBusiness("Acme", userDetails);
            Long bizB = createBusiness("Beta Corp", userDetails);

            createJobOffer(bizA, "Offre A1", userDetails);
            createJobOffer(bizB, "Offre B1", userDetails);
            createJobOffer(bizB, "Offre B2", userDetails);

            MvcResult result = mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

            var tree = objectMapper.readTree(result.getResponse().getContentAsString());
            // trouver le node business A
            var businessANode = null != tree.get(0)
                && tree.get(0).get("name").asText().equals("Acme")
                ? tree.get(0) : tree.get(1);

            assertThat(businessANode.get("jobOffersList").size()).isEqualTo(1);
            assertThat(businessANode.get("jobOffersList").get(0).get("name").asText()).isEqualTo("Offre A1");
        }

        @Test
        @DisplayName("401 — GET /api/business sans authentification")
        void tc005_getBusiness_shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(
                get("/api/business")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Un user sans business retourne un tableau vide")
        void tc005_userWithNoBusinesses_shouldReturnEmptyArray() throws Exception {
            mockMvc.perform(
                get("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
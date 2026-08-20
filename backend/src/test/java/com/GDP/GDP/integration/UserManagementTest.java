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
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.entity.User.Role;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.security.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;

/**
 * TC-010 — Gestion de compte : profil, mot de passe, suppression
 *   Topic : Sécurité et isolation
 *   Scénario : Un utilisateur authentifié consulte et modifie son propre profil
 *              (pseudo, email, mot de passe) ou supprime son compte.
 *
 *   Ce test valide :
 *     - Lecture du profil courant
 *     - Modification pseudo/email avec vérification du mot de passe actuel
 *     - Unicité de l'email au changement
 *     - Réémission du cookie JWT quand l'email (subject du token) change
 *     - Changement de mot de passe avec vérification de l'ancien
 *     - Suppression du compte protégée par mot de passe, avec cascade sur les businesses
 *     - Protection de toutes les routes par l'authentification
 */
@DisplayName("TC-010 — Gestion de compte utilisateur")
@SpringBootTest
@AutoConfigureMockMvc
public class UserManagementTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private User savedUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );
        savedUser = userRepository.save(
            new User("userA", "userA@mail.com", passwordEncoder.encode("password123"), Role.ROLE_USER)
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

    // =========================================================================
    // GET /api/user
    // =========================================================================
    @Nested
    @DisplayName("Consultation du profil — GET /api/user")
    class GetProfileTest {

        @Test
        @DisplayName("200 — Retourne le profil de l'utilisateur authentifié, sans le mot de passe")
        void tc010_getProfile_shouldReturnOwnProfile() throws Exception {
            mockMvc.perform(get("/api/user").with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("userA"))
                .andExpect(jsonPath("$.email").value("userA@mail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("401 — Sans authentification")
        void tc010_getProfile_withoutAuth_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/user
    // =========================================================================
    @Nested
    @DisplayName("Mise à jour du profil — PUT /api/user")
    class UpdateProfileTest {

        @Test
        @DisplayName("200 — Changement de pseudo seul : email inchangé, pas de nouveau cookie")
        void tc010_updateProfile_pseudoOnly_shouldNotReissueCookie() throws Exception {
            MvcResult result = mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("newPseudo", "userA@mail.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("newPseudo"))
                .andReturn();

            assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
            assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getPseudo()).isEqualTo("newPseudo");
        }

        @Test
        @DisplayName("200 — Changement d'email : un nouveau cookie 'token' est émis et l'ancien token ne fonctionne plus")
        void tc010_updateProfile_emailChanged_shouldReissueCookie() throws Exception {
            MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "password123"))))
                .andExpect(status().isNoContent())
                .andReturn();
            String oldCookie = extractCookieValue(loginResult.getResponse().getHeader("Set-Cookie"));

            MvcResult updateResult = mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("userA", "userA-new@mail.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("userA-new@mail.com"))
                .andReturn();

            String newCookie = extractCookieValue(updateResult.getResponse().getHeader("Set-Cookie"));
            assertThat(newCookie).isNotNull().isNotEqualTo(oldCookie);

            // L'ancien token (subject = ancien email) n'authentifie plus une fois l'email changé.
            mockMvc.perform(get("/api/user").cookie(new Cookie("token", oldCookie)))
                .andExpect(status().isUnauthorized());

            // Le nouveau token, lui, authentifie bien.
            mockMvc.perform(get("/api/user").cookie(new Cookie("token", newCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("userA-new@mail.com"));
        }

        @Test
        @DisplayName("401 — Mauvais mot de passe actuel : profil inchangé")
        void tc010_updateProfile_wrongPassword_shouldReturn401_andNotChangeProfile() throws Exception {
            mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("newPseudo", "userA@mail.com", "mauvaisMotDePasse"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

            assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getPseudo()).isEqualTo("userA");
        }

        @Test
        @DisplayName("409 — Email déjà utilisé par un autre utilisateur")
        void tc010_updateProfile_emailAlreadyTaken_shouldReturn409() throws Exception {
            userRepository.save(new User("userB", "userB@mail.com", passwordEncoder.encode("password123"), Role.ROLE_USER));

            mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("userA", "userB@mail.com", "password123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("200 — Changer l'email vers sa propre valeur actuelle n'est pas un conflit")
        void tc010_updateProfile_sameEmail_shouldNotConflict() throws Exception {
            mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("userA", "userA@mail.com", "password123"))))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 — Email malformé")
        void tc010_updateProfile_invalidEmail_shouldReturn400() throws Exception {
            mockMvc.perform(
                put("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("userA", "pas-un-email", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("401 — Sans authentification")
        void tc010_updateProfile_withoutAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                put("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserUpdateRequest("newPseudo", "userA@mail.com", "password123"))))
                .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/user/password
    // =========================================================================
    @Nested
    @DisplayName("Changement de mot de passe — PUT /api/user/password")
    class UpdatePasswordTest {

        @Test
        @DisplayName("204 — Mot de passe changé : login avec l'ancien échoue, login avec le nouveau réussit")
        void tc010_updatePassword_shouldChangePassword() throws Exception {
            mockMvc.perform(
                put("/api/user/password")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserPasswordUpdateRequest("password123", "newPassword456"))))
                .andExpect(status().isNoContent());

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "password123"))))
                .andExpect(status().isUnauthorized());

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "newPassword456"))))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("401 — Mauvais mot de passe actuel : mot de passe inchangé")
        void tc010_updatePassword_wrongCurrentPassword_shouldReturn401() throws Exception {
            mockMvc.perform(
                put("/api/user/password")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserPasswordUpdateRequest("mauvaisMotDePasse", "newPassword456"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "password123"))))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("400 — Nouveau mot de passe trop court")
        void tc010_updatePassword_tooShort_shouldReturn400() throws Exception {
            mockMvc.perform(
                put("/api/user/password")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserPasswordUpdateRequest("password123", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("401 — Sans authentification")
        void tc010_updatePassword_withoutAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                put("/api/user/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserPasswordUpdateRequest("password123", "newPassword456"))))
                .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // DELETE /api/user
    // =========================================================================
    @Nested
    @DisplayName("Suppression du compte — DELETE /api/user")
    class DeleteAccountTest {

        @Test
        @DisplayName("204 — Le compte et ses businesses associés sont supprimés, le cookie est vidé")
        void tc010_deleteAccount_shouldRemoveUser_andCascadeBusinesses() throws Exception {
            mockMvc.perform(
                post("/api/business")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new BusinessRequest("Acme", "desc", "contact"))))
                .andExpect(status().isCreated());
            assertThat(businessRepository.findAll()).hasSize(1);

            MvcResult result = mockMvc.perform(
                delete("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserDeleteRequest("password123"))))
                .andExpect(status().isNoContent())
                .andReturn();

            String setCookie = result.getResponse().getHeader("Set-Cookie");
            assertThat(setCookie).contains("token=;");
            assertThat(setCookie).containsIgnoringCase("Max-Age=0");

            assertThat(userRepository.findById(savedUser.getId())).isEmpty();
            assertThat(businessRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("401 — Mauvais mot de passe : compte non supprimé")
        void tc010_deleteAccount_wrongPassword_shouldReturn401_andKeepAccount() throws Exception {
            mockMvc.perform(
                delete("/api/user")
                    .with(user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserDeleteRequest("mauvaisMotDePasse"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

            assertThat(userRepository.findById(savedUser.getId())).isPresent();
        }

        @Test
        @DisplayName("401 — Sans authentification, compte non supprimé")
        void tc010_deleteAccount_withoutAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                delete("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new UserDeleteRequest("password123"))))
                .andExpect(status().isUnauthorized());

            assertThat(userRepository.findById(savedUser.getId())).isPresent();
        }
    }

    private String extractCookieValue(String setCookieHeader) {
        // "token=<value>; Path=/; ..." → juste la valeur du cookie.
        String withoutName = setCookieHeader.substring("token=".length());
        int separatorIndex = withoutName.indexOf(';');
        return separatorIndex == -1 ? withoutName : withoutName.substring(0, separatorIndex);
    }
}

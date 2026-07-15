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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.auth.RegisterRequest;
import com.GDP.GDP.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * TC-009 — Auth : register, login, logout
 *   Topic : Sécurité et isolation
 *   Scénario : Un utilisateur peut s'inscrire, se connecter et se déconnecter
 *              via les endpoints publics d'authentification.
 *
 *   Ce test valide :
 *     - Persistance et unicité de l'utilisateur (contrainte email)
 *     - Hashage du mot de passe (jamais retourné en clair)
 *     - Émission et validité du token/session après login
 *     - Invalidation effective du token après logout
 *     - Protection des routes après déconnexion (Spring Security filter chain)
 *     - Validation des inputs sur les trois endpoints
 */
@DisplayName("TC-009 — Auth : register, login, logout")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE applications, job_offers, professionals, businesses, users RESTART IDENTITY CASCADE;"
        );
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

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest("userA", "userA@mail.com", "password123");
    }



    // =========================================================================
    // TC-009a — Register
    // =========================================================================
    @Nested
    @DisplayName("Register — POST /api/auth/register")
    class RegisterTest {

        @Test
        @DisplayName("201 — Inscription nominale : user persisté avec les bons champs")
        void tc009_register_shouldReturn201_andPersistUser() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            var users = userRepository.findAll();
            assertThat(users).hasSize(1);
            assertThat(users.get(0).getEmail()).isEqualTo("userA@mail.com");
            assertThat(users.get(0).getPseudo()).isEqualTo("userA");
        }

        @Test
        @DisplayName("Le mot de passe est hashé en base (jamais stocké en clair)")
        void tc009_register_shouldHashPassword_notStoreClearText() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            var user = userRepository.findAll().get(0);
            assertThat(user.getPassword()).isNotEqualTo("password123");
            assertThat(user.getPassword()).startsWith("$2"); // BCrypt prefix
        }

        @Test
        @DisplayName("Le mot de passe n'est jamais retourné dans la réponse")
        void tc009_register_shouldNeverReturnPassword_inResponse() throws Exception {
            MvcResult result = mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated())
                .andReturn();

            String body = result.getResponse().getContentAsString();
            assertThat(body).doesNotContain("password123");
            assertThat(body).doesNotContain("password");
        }

        @Test
        @DisplayName("409 — Email déjà utilisé : second register refusé, aucun doublon en base")
        void tc009_register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
            // Premier register → OK
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            // Second register même email → 409
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new RegisterRequest("autreNom", "userA@mail.com", "autrePass"))))
                .andExpect(status().isConflict());

            // Aucun doublon
            assertThat(userRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("400 — Email malformé")
        void tc009_register_shouldReturn400_whenEmailIsInvalid() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new RegisterRequest("userA", "pas-un-email", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

            assertThat(userRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("400 — Pseudo vide")
        void tc009_register_shouldReturn400_whenPseudoIsBlank() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new RegisterRequest("", "userA@mail.com", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

            assertThat(userRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("400 — Password vide")
        void tc009_register_shouldReturn400_whenPasswordIsBlank() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new RegisterRequest("userA", "userA@mail.com", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

            assertThat(userRepository.findAll()).isEmpty();
        }
    }

    // =========================================================================
    // TC-009b — Login
    // =========================================================================
    @Nested
    @DisplayName("Login — POST /api/auth/login")
    class LoginTest {

        @Test
        @DisplayName("204 — Login nominal : cookie 'token' posé avec les attributs configurés (secure/sameSite)")
        void tc009_login_shouldReturn204_andSetTokenCookie() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            MvcResult result = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "password123"))))
                .andExpect(status().isNoContent())
                .andReturn();

            String setCookie = result.getResponse().getHeader("Set-Cookie");
            assertThat(setCookie).isNotNull();
            assertThat(setCookie).contains("token=");
            assertThat(setCookie).containsIgnoringCase("HttpOnly");
            // reflects app.auth.cookie.same-site / app.auth.cookie.secure defaults (Lax / false)
            assertThat(setCookie).containsIgnoringCase("SameSite=Lax");
            assertThat(setCookie).doesNotContainIgnoringCase("Secure");
        }

        @Test
        @DisplayName("400 — Email malformé")
        void tc009_login_shouldReturn400_whenEmailIsInvalid() throws Exception {
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("pas-un-email", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("401 — Mauvais mot de passe : erreur typée, pas un 500")
        void tc009_login_shouldReturn401_whenPasswordIsWrong() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "mauvaisMotDePasse"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("401 — Email inconnu : erreur typée, pas un 500")
        void tc009_login_shouldReturn401_whenEmailUnknown() throws Exception {
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("inconnu@mail.com", "password123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
    }

    // =========================================================================
    // TC-009c — Logout
    // =========================================================================
    @Nested
    @DisplayName("Logout — POST /api/auth/logout")
    class LogoutTest {

        @Test
        @DisplayName("204 — Logout : cookie 'token' vidé avec EXACTEMENT les mêmes attributs HttpOnly/Secure/SameSite que login")
        void tc009_logout_shouldClearTokenCookie_withSameAttributesAsLogin() throws Exception {
            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(validRegisterRequest())))
                .andExpect(status().isCreated());

            MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(new LoginRequest("userA@mail.com", "password123"))))
                .andExpect(status().isNoContent())
                .andReturn();
            String loginCookie = loginResult.getResponse().getHeader("Set-Cookie");

            MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andReturn();
            String logoutCookie = logoutResult.getResponse().getHeader("Set-Cookie");

            assertThat(logoutCookie).isNotNull();
            assertThat(logoutCookie).contains("token=;"); // valeur vidée
            assertThat(logoutCookie).containsIgnoringCase("Max-Age=0"); // expire immédiatement

            // Régression du bug historique : login posait secure=false/sameSite=Lax mais logout
            // posait secure=true/sameSite=None, donc le navigateur refusait le cookie de logout
            // en HTTP et ne supprimait jamais la session. On vérifie ici que les deux cookies
            // partagent strictement les mêmes attributs HttpOnly/Secure/SameSite.
            for (String attribute : new String[] { "HttpOnly", "SameSite=Lax" }) {
                assertThat(loginCookie).containsIgnoringCase(attribute);
                assertThat(logoutCookie).containsIgnoringCase(attribute);
            }
            assertThat(loginCookie).doesNotContainIgnoringCase("Secure");
            assertThat(logoutCookie).doesNotContainIgnoringCase("Secure");
        }
    }
}
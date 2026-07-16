package com.GDP.GDP.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
        "dGVzdC1zZWNyZXQta2V5LWZvci1qd3Qtc2VydmljZS11bml0LXRlc3RzLW9ubHktbm90LXJlYWw=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Nested
    @DisplayName("generateToken() / extractUsername()")
    class GenerateAndExtract {

        @Test
        @DisplayName("should round-trip the username through the token's subject")
        void shouldRoundTripUsername() {
            String token = jwtService.generateToken("user@test.com");

            assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("should return true when username matches and token is not expired")
        void shouldReturnTrueForMatchingUsername() {
            String token = jwtService.generateToken("user@test.com");
            UserDetails details = mock(UserDetails.class);
            when(details.getUsername()).thenReturn("user@test.com");

            assertThat(jwtService.isTokenValid(token, details)).isTrue();
        }

        @Test
        @DisplayName("should return false when username does not match")
        void shouldReturnFalseWhenUsernameDoesNotMatch() {
            String token = jwtService.generateToken("user@test.com");
            UserDetails details = mock(UserDetails.class);
            when(details.getUsername()).thenReturn("someone-else@test.com");

            assertThat(jwtService.isTokenValid(token, details)).isFalse();
        }

        @Test
        @DisplayName("should throw ExpiredJwtException for an expired token (JJWT validates exp at parse time)")
        void shouldThrowWhenTokenExpired() {
            ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
            String token = jwtService.generateToken("user@test.com");
            UserDetails details = mock(UserDetails.class);
            when(details.getUsername()).thenReturn("user@test.com");

            assertThatThrownBy(() -> jwtService.isTokenValid(token, details))
                .isInstanceOf(ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("token integrity")
    class TokenIntegrity {

        @Test
        @DisplayName("should fail to parse a token signed with a different secret")
        void shouldRejectTokenSignedWithDifferentSecret() {
            String token = jwtService.generateToken("user@test.com");

            JwtService otherService = new JwtService();
            ReflectionTestUtils.setField(
                otherService, "SECRET_KEY",
                "YW5vdGhlci1jb21wbGV0ZWx5LWRpZmZlcmVudC1zZWNyZXQta2V5LWZvci10ZXN0cw=="
            );
            ReflectionTestUtils.setField(otherService, "expiration", 3600000L);

            assertThatThrownBy(() -> otherService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
        }
    }
}

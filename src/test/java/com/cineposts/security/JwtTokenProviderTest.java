package com.cineposts.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", EXPIRATION);
    }

    private Authentication buildAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(
                username, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("deve gerar token não nulo e não vazio")
        void generateToken_returnsNonBlankToken() {
            Authentication auth = buildAuthentication("joao");
            String token = tokenProvider.generateToken(auth);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("deve gerar tokens diferentes para usuários diferentes")
        void generateToken_differentUsersGetDifferentTokens() {
            String tokenJoao  = tokenProvider.generateToken(buildAuthentication("joao"));
            String tokenMaria = tokenProvider.generateToken(buildAuthentication("maria"));
            assertThat(tokenJoao).isNotEqualTo(tokenMaria);
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    class GetUsernameFromToken {

        @Test
        @DisplayName("deve extrair username correto do token")
        void getUsernameFromToken_returnsCorrectUsername() {
            Authentication auth = buildAuthentication("joao");
            String token = tokenProvider.generateToken(auth);

            String username = tokenProvider.getUsernameFromToken(token);

            assertThat(username).isEqualTo("joao");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("deve retornar true para token válido")
        void validateToken_validToken_returnsTrue() {
            String token = tokenProvider.generateToken(buildAuthentication("joao"));
            assertThat(tokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para token malformado")
        void validateToken_malformedToken_returnsFalse() {
            assertThat(tokenProvider.validateToken("token.invalido.aqui")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token vazio")
        void validateToken_emptyToken_returnsFalse() {
            assertThat(tokenProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token expirado")
        void validateToken_expiredToken_returnsFalse() {
            // Gera token com expiração negativa (já expirado)
            JwtTokenProvider expiredProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(expiredProvider, "jwtSecret", SECRET);
            ReflectionTestUtils.setField(expiredProvider, "jwtExpirationMs", -1000L);

            String expiredToken = expiredProvider.generateToken(buildAuthentication("joao"));

            assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
        }
    }
}

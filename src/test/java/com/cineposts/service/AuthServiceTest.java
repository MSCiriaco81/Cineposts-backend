package com.cineposts.service;

import com.cineposts.dto.request.LoginRequest;
import com.cineposts.dto.response.AuthResponse;
import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.UserRepository;
import com.cineposts.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks private AuthService authService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id("user-001")
                .username("admin")
                .password("hashed-password")
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("deve retornar token JWT ao fazer login com credenciais válidas")
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("deve lançar exceção ao fazer login com credenciais inválidas")
    void login_invalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenProvider, never()).generateToken(any());
    }
}

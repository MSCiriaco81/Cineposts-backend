package com.cineposts.service;

import com.cineposts.dto.request.CreateUserRequest;
import com.cineposts.dto.request.UpdateUserRequest;
import com.cineposts.dto.response.UserResponse;
import com.cineposts.exception.BusinessRuleException;
import com.cineposts.exception.ResourceNotFoundException;
import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService")
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AdminService adminService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id("user-001")
                .username("joao")
                .password("hashed")
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("deve criar usuário com sucesso")
        void createUser_success() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("maria");
            request.setPassword("senha123");
            request.setRole(Role.USER);

            when(userRepository.existsByUsername("maria")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("hashed-senha");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId("new-id");
                return u;
            });

            UserResponse response = adminService.createUser(request);

            assertThat(response.getUsername()).isEqualTo("maria");
            assertThat(response.getRole()).isEqualTo(Role.USER);
            assertThat(response.isActive()).isTrue();
            verify(passwordEncoder).encode("senha123");
        }

        @Test
        @DisplayName("deve lançar exceção quando username já existe")
        void createUser_duplicateUsername_throwsException() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("joao");
            request.setPassword("senha123");
            request.setRole(Role.USER);

            when(userRepository.existsByUsername("joao")).thenReturn(true);

            assertThatThrownBy(() -> adminService.createUser(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("joao");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("deve retornar lista de todos os usuários")
        void getAllUsers_returnsList() {
            when(userRepository.findAll()).thenReturn(List.of(existingUser));

            List<UserResponse> result = adminService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("joao");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há usuários")
        void getAllUsers_empty_returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponse> result = adminService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("deve atualizar papel do usuário")
        void updateUser_updatesRole() {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setRole(Role.ADMIN);

            when(userRepository.findById("user-001")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any())).thenReturn(existingUser);

            UserResponse response = adminService.updateUser("user-001", request);

            assertThat(existingUser.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("deve atualizar senha do usuário")
        void updateUser_updatesPassword() {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setPassword("nova-senha");

            when(userRepository.findById("user-001")).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode("nova-senha")).thenReturn("new-hashed");
            when(userRepository.save(any())).thenReturn(existingUser);

            adminService.updateUser("user-001", request);

            verify(passwordEncoder).encode("nova-senha");
            assertThat(existingUser.getPassword()).isEqualTo("new-hashed");
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não existe")
        void updateUser_notFound_throwsException() {
            when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.updateUser("invalid-id", new UpdateUserRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deve desativar usuário (soft delete)")
        void deleteUser_deactivatesUser() {
            when(userRepository.findById("user-001")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any())).thenReturn(existingUser);

            adminService.deleteUser("user-001");

            assertThat(existingUser.isActive()).isFalse();
            verify(userRepository).save(existingUser);
            verify(userRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("deve lançar exceção quando usuário não existe")
        void deleteUser_notFound_throwsException() {
            when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.deleteUser("invalid-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

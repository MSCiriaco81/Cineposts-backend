package com.cineposts.controller;

import com.cineposts.dto.request.CreateUserRequest;
import com.cineposts.dto.response.UserResponse;
import com.cineposts.model.enums.Role;
import com.cineposts.service.AdminService;
import com.cineposts.service.EditRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AdminController")
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminService adminService;
    @MockBean private EditRequestService editRequestService;

    private UserResponse buildUserResponse(String username, Role role) {
        UserResponse r = new UserResponse();
        r.setId("user-001");
        r.setUsername(username);
        r.setRole(role);
        r.setActive(true);
        return r;
    }

    // ── Acesso negado para USER ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /admin/users — deve retornar 403 para papel USER")
    void getUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/users — deve retornar 401 sem autenticação")
    void getUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ── Acesso permitido para ADMIN ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /admin/users — deve retornar 200 e lista de usuários para ADMIN")
    void getUsers_asAdmin_returns200() throws Exception {
        when(adminService.getAllUsers())
                .thenReturn(List.of(
                        buildUserResponse("joao", Role.USER),
                        buildUserResponse("maria", Role.USER)
                ));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("joao"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/users — deve retornar 201 ao criar usuário válido")
    void createUser_asAdmin_returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("novo");
        request.setPassword("senha123");
        request.setRole(Role.USER);

        when(adminService.createUser(any())).thenReturn(buildUserResponse("novo", Role.USER));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("novo"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /admin/users — deve retornar 400 quando username está ausente")
    void createUser_missingUsername_returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setPassword("senha123");
        request.setRole(Role.USER);
        // username ausente

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("DELETE /admin/users/{id} — deve retornar 204 ao desativar usuário")
    void deleteUser_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/admin/users/user-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("DELETE /admin/users/{id} — deve retornar 403 para papel USER")
    void deleteUser_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/admin/users/user-001"))
                .andExpect(status().isForbidden());
    }
}

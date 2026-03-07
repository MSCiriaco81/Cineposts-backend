package com.cineposts.controller;

import com.cineposts.dto.request.CreateContentRequest;
import com.cineposts.dto.response.ContentResponse;
import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import com.cineposts.service.ContentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ContentController")
class ContentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ContentService contentService;

    private ContentResponse buildContentResponse() {
        ContentResponse r = new ContentResponse();
        r.setId("content-001");
        r.setType(ContentType.MOVIE_ANNIVERSARY);
        r.setTitle("Matrix");
        r.setDescription("Clássico de ficção científica");
        r.setStatus(ContentStatus.PENDING);
        r.setCreatedByUsername("joao");
        return r;
    }

    @Test
    @DisplayName("GET /contents — deve retornar 401 sem autenticação")
    void getContents_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/contents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /contents — deve retornar 200 e lista de conteúdos")
    void getContents_authenticated_returns200() throws Exception {
        when(contentService.getAll()).thenReturn(List.of(buildContentResponse()));

        mockMvc.perform(get("/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("content-001"))
                .andExpect(jsonPath("$[0].title").value("Matrix"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /contents — deve retornar lista vazia quando não há conteúdos")
    void getContents_empty_returnsEmptyList() throws Exception {
        when(contentService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("POST /contents — deve retornar 201 ao criar conteúdo válido")
    void createContent_valid_returns201() throws Exception {
        CreateContentRequest request = new CreateContentRequest();
        request.setType(ContentType.MOVIE_ANNIVERSARY);
        request.setTitle("Matrix");
        request.setDescription("Clássico de ficção científica");

        when(contentService.create(any(), eq("joao"))).thenReturn(buildContentResponse());

        mockMvc.perform(post("/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Matrix"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("POST /contents — deve retornar 400 quando título está ausente")
    void createContent_missingTitle_returns400() throws Exception {
        CreateContentRequest request = new CreateContentRequest();
        request.setType(ContentType.TRIVIA);
        // título ausente

        mockMvc.perform(post("/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /contents/{id} — deve retornar 200 quando conteúdo existe")
    void getContentById_exists_returns200() throws Exception {
        when(contentService.getById("content-001")).thenReturn(buildContentResponse());

        mockMvc.perform(get("/contents/content-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("content-001"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("DELETE /contents/{id} — deve retornar 204 ao deletar com sucesso")
    void deleteContent_returns204() throws Exception {
        mockMvc.perform(delete("/contents/content-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /contents — deve retornar 401 sem autenticação")
    void createContent_unauthenticated_returns401() throws Exception {
        CreateContentRequest request = new CreateContentRequest();
        request.setType(ContentType.TRIVIA);
        request.setTitle("Qualquer");
        request.setDescription("Descrição");

        mockMvc.perform(post("/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

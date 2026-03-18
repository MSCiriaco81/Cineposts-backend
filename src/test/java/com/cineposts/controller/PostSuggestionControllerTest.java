package com.cineposts.controller;

import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.model.ImageAsset;
import com.cineposts.model.enums.Platform;
import com.cineposts.model.enums.SuggestionStatus;
import com.cineposts.service.PostSuggestionImageService;
import com.cineposts.service.PostSuggestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PostSuggestionController")
class PostSuggestionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PostSuggestionService postSuggestionService;
        @MockBean private PostSuggestionImageService postSuggestionImageService;

    private PostSuggestionResponse buildSuggestion(Platform platform) {
        PostSuggestionResponse r = new PostSuggestionResponse();
        r.setId("sug-001");
        r.setContentId("content-001");
        r.setPlatform(platform);
        r.setHook("Hook de teste");
        r.setCaption("Caption de teste");
        r.setHashtags(List.of("#cinema", "#teste"));
        r.setCta("Comentem abaixo!");
        r.setImages(List.of(ImageAsset.builder()
                .url("https://fake-storage.cineposts.dev/cineposts/demo.png")
                .publicId("cineposts/demo")
                .format("png")
                .width(1080)
                .height(1080)
                .sizeKb(321.45)
                .build()));
        r.setStatus(SuggestionStatus.DRAFT);
        r.setCreatedByUsername("joao");
        return r;
    }

    @Test
    @DisplayName("POST /contents/{id}/suggestions/twitter — deve retornar 401 sem autenticação")
    void generateTwitter_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/contents/content-001/suggestions/twitter"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("POST /contents/{id}/suggestions/twitter — deve retornar 201 com sugestão gerada")
    void generateTwitter_authenticated_returns201() throws Exception {
        when(postSuggestionService.generateTwitter(eq("content-001"), eq("joao")))
                .thenReturn(buildSuggestion(Platform.TWITTER));

        mockMvc.perform(post("/contents/content-001/suggestions/twitter"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.platform").value("TWITTER"))
                .andExpect(jsonPath("$.hook").value("Hook de teste"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("POST /contents/{id}/suggestions/instagram — deve retornar 201 com sugestão gerada")
    void generateInstagram_authenticated_returns201() throws Exception {
        when(postSuggestionService.generateInstagram(eq("content-001"), eq("joao")))
                .thenReturn(buildSuggestion(Platform.INSTAGRAM));

        mockMvc.perform(post("/contents/content-001/suggestions/instagram"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.platform").value("INSTAGRAM"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /post-suggestions — deve retornar 200 e lista de sugestões")
    void getSuggestions_authenticated_returns200() throws Exception {
        when(postSuggestionService.getAll())
                .thenReturn(List.of(
                        buildSuggestion(Platform.TWITTER),
                        buildSuggestion(Platform.INSTAGRAM)
                ));

        mockMvc.perform(get("/post-suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /post-suggestions — deve retornar 401 sem autenticação")
    void getSuggestions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/post-suggestions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /post-suggestions/{id} — deve retornar 200 quando sugestão existe")
    void getSuggestionById_exists_returns200() throws Exception {
        when(postSuggestionService.getById("sug-001"))
                .thenReturn(buildSuggestion(Platform.TWITTER));

        mockMvc.perform(get("/post-suggestions/sug-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sug-001"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("POST /post-suggestions/{id}/images — deve retornar 201 com imagem adicionada")
    void uploadImage_authenticated_returns201() throws Exception {
        PostSuggestionResponse response = buildSuggestion(Platform.TWITTER);
        when(postSuggestionImageService.addImage(eq("sug-001"), any(), eq("joao")))
                .thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/post-suggestions/sug-001/images").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.images[0].format").value("png"));
    }

    @Test
    @DisplayName("POST /post-suggestions/{id}/images — deve retornar 401 sem autenticação")
    void uploadImage_unauthenticated_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/post-suggestions/sug-001/images").file(file))
                .andExpect(status().isUnauthorized());
    }
}

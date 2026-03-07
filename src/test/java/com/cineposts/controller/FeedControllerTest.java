package com.cineposts.controller;

import com.cineposts.dto.response.ContentResponse;
import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import com.cineposts.model.enums.Platform;
import com.cineposts.model.enums.SuggestionStatus;
import com.cineposts.service.FeedService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("FeedController")
class FeedControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private FeedService feedService;

    @Test
    @DisplayName("GET /team/contents — deve retornar 401 sem autenticação")
    void getFeedContents_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/team/contents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /team/contents — deve retornar 200 com conteúdos da equipe")
    void getFeedContents_authenticated_returns200() throws Exception {
        ContentResponse c = new ContentResponse();
        c.setId("content-001");
        c.setTitle("Matrix");
        c.setType(ContentType.MOVIE_ANNIVERSARY);
        c.setStatus(ContentStatus.APPROVED);
        c.setCreatedByUsername("joao");

        when(feedService.getTeamContentFeed()).thenReturn(List.of(c));

        mockMvc.perform(get("/team/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Matrix"))
                .andExpect(jsonPath("$[0].createdByUsername").value("joao"));
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /team/post-suggestions — deve retornar 200 com sugestões da equipe")
    void getFeedSuggestions_authenticated_returns200() throws Exception {
        PostSuggestionResponse s = new PostSuggestionResponse();
        s.setId("sug-001");
        s.setPlatform(Platform.TWITTER);
        s.setHook("Hook");
        s.setStatus(SuggestionStatus.READY);
        s.setCreatedByUsername("maria");

        when(feedService.getTeamSuggestionFeed()).thenReturn(List.of(s));

        mockMvc.perform(get("/team/post-suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].platform").value("TWITTER"))
                .andExpect(jsonPath("$[0].createdByUsername").value("maria"));
    }

    @Test
    @DisplayName("GET /team/post-suggestions — deve retornar 401 sem autenticação")
    void getFeedSuggestions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/team/post-suggestions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "joao", roles = "USER")
    @DisplayName("GET /team/contents — deve retornar lista vazia quando equipe não tem conteúdo")
    void getFeedContents_empty_returnsEmptyList() throws Exception {
        when(feedService.getTeamContentFeed()).thenReturn(List.of());

        mockMvc.perform(get("/team/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}

package com.cineposts.service;

import com.cineposts.model.Content;
import com.cineposts.model.PostSuggestion;
import com.cineposts.model.enums.ContentType;
import com.cineposts.model.enums.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PostGeneratorService")
class PostGeneratorServiceTest {

    private PostGeneratorService generator;

    @BeforeEach
    void setUp() {
        generator = new PostGeneratorService();
    }

    private Content buildContent(ContentType type, String title, LocalDate eventDate, List<String> tags) {
        return Content.builder()
                .id("content-001")
                .type(type)
                .title(title)
                .description("Descrição do conteúdo para testes.")
                .eventDate(eventDate)
                .tags(tags)
                .build();
    }

    @Nested
    @DisplayName("generateTwitterPost")
    class TwitterPost {

        @Test
        @DisplayName("deve gerar post para plataforma TWITTER")
        void generateTwitterPost_platformIsTwitter() {
            Content content = buildContent(ContentType.MOVIE_ANNIVERSARY, "Matrix",
                    LocalDate.of(1999, 3, 31), List.of("scifi"));

            PostSuggestion suggestion = generator.generateTwitterPost(content, "user-001", "joao");

            assertThat(suggestion.getPlatform()).isEqualTo(Platform.TWITTER);
        }

        @Test
        @DisplayName("deve incluir o título no hook do post de aniversário")
        void generateTwitterPost_movieAnniversary_hookContainsTitle() {
            Content content = buildContent(ContentType.MOVIE_ANNIVERSARY, "Matrix",
                    LocalDate.of(1999, 3, 31), null);

            PostSuggestion suggestion = generator.generateTwitterPost(content, "user-001", "joao");

            assertThat(suggestion.getHook()).contains("Matrix");
        }

        @Test
        @DisplayName("deve gerar no máximo 3 hashtags para Twitter")
        void generateTwitterPost_maxThreeHashtags() {
            Content content = buildContent(ContentType.MOVIE_ANNIVERSARY, "Matrix",
                    LocalDate.of(1999, 3, 31), List.of("scifi", "acao", "classioco"));

            PostSuggestion suggestion = generator.generateTwitterPost(content, "user-001", "joao");

            assertThat(suggestion.getHashtags()).hasSizeLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("deve incluir CTA no post Twitter")
        void generateTwitterPost_hasCta() {
            Content content = buildContent(ContentType.TRIVIA, "Curiosidade",
                    null, null);

            PostSuggestion suggestion = generator.generateTwitterPost(content, "user-001", "joao");

            assertThat(suggestion.getCta()).isNotBlank();
        }

        @Test
        @DisplayName("deve registrar o criador corretamente")
        void generateTwitterPost_setsCreatedBy() {
            Content content = buildContent(ContentType.TRIVIA, "Curiosidade", null, null);

            PostSuggestion suggestion = generator.generateTwitterPost(content, "user-001", "joao");

            assertThat(suggestion.getCreatedBy()).isEqualTo("user-001");
            assertThat(suggestion.getCreatedByUsername()).isEqualTo("joao");
        }
    }

    @Nested
    @DisplayName("generateInstagramPost")
    class InstagramPost {

        @Test
        @DisplayName("deve gerar post para plataforma INSTAGRAM")
        void generateInstagramPost_platformIsInstagram() {
            Content content = buildContent(ContentType.RECOMMENDATION, "Parasita",
                    null, null);

            PostSuggestion suggestion = generator.generateInstagramPost(content, "user-001", "joao");

            assertThat(suggestion.getPlatform()).isEqualTo(Platform.INSTAGRAM);
        }

        @Test
        @DisplayName("deve gerar entre 5 e 12 hashtags para Instagram")
        void generateInstagramPost_hashtagCountInRange() {
            Content content = buildContent(ContentType.MOVIE_ANNIVERSARY, "Parasita",
                    LocalDate.of(2019, 5, 30), List.of("coreano", "drama"));

            PostSuggestion suggestion = generator.generateInstagramPost(content, "user-001", "joao");

            assertThat(suggestion.getHashtags().size()).isBetween(5, 12);
        }

        @Test
        @DisplayName("deve incluir o título no hook do Instagram")
        void generateInstagramPost_hookContainsTitle() {
            Content content = buildContent(ContentType.BEHIND_THE_SCENES, "Interestelar",
                    null, null);

            PostSuggestion suggestion = generator.generateInstagramPost(content, "user-001", "joao");

            assertThat(suggestion.getHook()).contains("Interestelar");
        }

        @Test
        @DisplayName("deve incluir CTA no post Instagram")
        void generateInstagramPost_hasCta() {
            Content content = buildContent(ContentType.COMPARISON, "Marvel vs DC", null, null);

            PostSuggestion suggestion = generator.generateInstagramPost(content, "user-001", "joao");

            assertThat(suggestion.getCta()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("cálculo de anos")
    class YearCalculation {

        @Test
        @DisplayName("deve calcular corretamente os anos desde o evento")
        void twitterPost_anniversaryYearsInHook() {
            int yearOfRelease = 1994;
            Content content = buildContent(ContentType.MOVIE_ANNIVERSARY, "Forrest Gump",
                    LocalDate.of(yearOfRelease, 7, 6), null);

            PostSuggestion suggestion = generator.generateTwitterPost(content, "u1", "user");

            int expectedYears = java.time.Year.now().getValue() - yearOfRelease;
            assertThat(suggestion.getHook()).contains(String.valueOf(expectedYears));
        }

        @Test
        @DisplayName("deve funcionar sem data de evento")
        void twitterPost_noEventDate_doesNotThrow() {
            Content content = buildContent(ContentType.TRIVIA, "Curiosidade", null, null);

            assertThatNoException().isThrownBy(() ->
                    generator.generateTwitterPost(content, "u1", "user"));
        }
    }

    @Nested
    @DisplayName("todos os tipos de conteúdo")
    class AllContentTypes {

        @Test
        @DisplayName("deve gerar post para todos os tipos de conteúdo no Twitter")
        void allContentTypes_twitterGeneratesWithoutException() {
            for (ContentType type : ContentType.values()) {
                Content content = buildContent(type, "Título Teste", null, null);
                assertThatNoException()
                        .as("Falhou para o tipo: " + type)
                        .isThrownBy(() -> generator.generateTwitterPost(content, "u1", "user"));
            }
        }

        @Test
        @DisplayName("deve gerar post para todos os tipos de conteúdo no Instagram")
        void allContentTypes_instagramGeneratesWithoutException() {
            for (ContentType type : ContentType.values()) {
                Content content = buildContent(type, "Título Teste", null, null);
                assertThatNoException()
                        .as("Falhou para o tipo: " + type)
                        .isThrownBy(() -> generator.generateInstagramPost(content, "u1", "user"));
            }
        }
    }
}

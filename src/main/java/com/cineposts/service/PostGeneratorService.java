package com.cineposts.service;

import com.cineposts.model.Content;
import com.cineposts.model.PostSuggestion;
import com.cineposts.model.enums.ContentType;
import com.cineposts.model.enums.Platform;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based post generator for MVP.
 *
 * Design rationale: separated from PostSuggestionService intentionally.
 * When an AI-based generator is implemented, it can replace or extend this
 * service without touching orchestration logic.
 */
@Service
public class PostGeneratorService {

    public PostSuggestion generateTwitterPost(Content content, String userId, String username) {
        String title = content.getTitle();
        int years = calculateYears(content.getEventDate());

        String hook = buildTwitterHook(content, title, years);
        String caption = buildTwitterCaption(content, title, years);
        List<String> hashtags = buildTwitterHashtags(content);
        String cta = buildTwitterCta(content);

        return PostSuggestion.builder()
                .contentId(content.getId())
                .platform(Platform.TWITTER)
                .hook(hook)
                .caption(caption)
                .hashtags(hashtags)
                .cta(cta)
                .createdBy(userId)
                .createdByUsername(username)
                .build();
    }

    public PostSuggestion generateInstagramPost(Content content, String userId, String username) {
        String title = content.getTitle();
        int years = calculateYears(content.getEventDate());

        String hook = buildInstagramHook(content, title, years);
        String caption = buildInstagramCaption(content, title, years);
        List<String> hashtags = buildInstagramHashtags(content);
        String cta = buildInstagramCta(content);

        return PostSuggestion.builder()
                .contentId(content.getId())
                .platform(Platform.INSTAGRAM)
                .hook(hook)
                .caption(caption)
                .hashtags(hashtags)
                .cta(cta)
                .createdBy(userId)
                .createdByUsername(username)
                .build();
    }

    // ---- Twitter builders ----

    private String buildTwitterHook(Content content, String title, int years) {
        return switch (content.getType()) {
            case MOVIE_ANNIVERSARY -> years > 0
                    ? "🎬 Hoje faz %d anos de \"%s\"!".formatted(years, title)
                    : "🎬 Lembrando de \"%s\"!".formatted(title);
            case SERIES_ANNIVERSARY, TV_SHOW_ANNIVERSARY -> years > 0
                    ? "📺 %d anos desde a estreia de \"%s\"!".formatted(years, title)
                    : "📺 Celebrando \"%s\"!".formatted(title);
            case PERSON_BIRTHDAY -> "🎂 Hoje é aniversário de %s!".formatted(
                    content.getRelatedPerson() != null ? content.getRelatedPerson() : title);
            case TRIVIA -> "🎥 Você sabia? Sobre \"%s\":".formatted(title);
            case BEHIND_THE_SCENES -> "🎬 Bastidores de \"%s\":".formatted(title);
            case RELEASE_REMINDER -> "📅 Lembrete: \"%s\" chega em breve!".formatted(title);
            case RECOMMENDATION -> "⭐ Recomendação do dia:".formatted();
            case COMPARISON -> "⚔️ Comparando:".formatted();
        };
    }

    private String buildTwitterCaption(Content content, String title, int years) {
        String description = truncate(content.getDescription(), 180);
        return description;
    }

    private String buildTwitterCta(Content content) {
        return switch (content.getType()) {
            case MOVIE_ANNIVERSARY, SERIES_ANNIVERSARY, TV_SHOW_ANNIVERSARY ->
                    "Qual sua cena favorita? 👇";
            case PERSON_BIRTHDAY ->
                    "Qual seu filme favorito dele/dela? 👇";
            case TRIVIA, BEHIND_THE_SCENES ->
                    "Você já sabia disso? 👇";
            case RELEASE_REMINDER ->
                    "Você vai assistir? 👇";
            case RECOMMENDATION ->
                    "Já assistiu? O que achou? 👇";
            case COMPARISON ->
                    "Com qual você fica? 👇";
        };
    }

    private List<String> buildTwitterHashtags(Content content) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add("#cinema");
        if (content.getType() == ContentType.PERSON_BIRTHDAY) {
            hashtags.add("#cinema");
        } else if (content.getType() == ContentType.SERIES_ANNIVERSARY
                || content.getType() == ContentType.TV_SHOW_ANNIVERSARY) {
            hashtags.add("#series");
        }
        if (content.getTags() != null && !content.getTags().isEmpty()) {
            hashtags.add("#" + content.getTags().getFirst().replace(" ", "").toLowerCase());
        }
        // Twitter: max 3 hashtags
        return hashtags.stream().distinct().limit(3).toList();
    }

    // ---- Instagram builders ----

    private String buildInstagramHook(Content content, String title, int years) {
        return switch (content.getType()) {
            case MOVIE_ANNIVERSARY -> years > 0
                    ? "🎬 Hoje faz %d anos de \"%s\" — um marco do cinema!".formatted(years, title)
                    : "🎬 Celebrando a obra prima \"%s\"!".formatted(title);
            case SERIES_ANNIVERSARY, TV_SHOW_ANNIVERSARY -> years > 0
                    ? "📺 %d anos desde que \"%s\" estreou e mudou tudo.".formatted(years, title)
                    : "📺 Uma série que ficou na memória: \"%s\".".formatted(title);
            case PERSON_BIRTHDAY -> "🎂 Hoje celebramos o aniversário de %s!".formatted(
                    content.getRelatedPerson() != null ? content.getRelatedPerson() : title);
            case TRIVIA -> "🎥 Curiosidade que poucos sabem sobre \"%s\":".formatted(title);
            case BEHIND_THE_SCENES ->
                    "🎬 Os bastidores contam histórias incríveis. Veja o que aconteceu em \"%s\":".formatted(title);
            case RELEASE_REMINDER ->
                    "📅 Marque na agenda! \"%s\" chega em breve!".formatted(title);
            case RECOMMENDATION ->
                    "⭐ Nossa recomendação de hoje é imperdível:".formatted();
            case COMPARISON ->
                    "⚔️ Um duelo clássico que divide opiniões:".formatted();
        };
    }

    private String buildInstagramCaption(Content content, String title, int years) {
        String description = content.getDescription();
        return "\n" + description + "\n\n" +
                "Um dos títulos que marcaram a história do entretenimento.\n";
    }

    private String buildInstagramCta(Content content) {
        return switch (content.getType()) {
            case MOVIE_ANNIVERSARY, SERIES_ANNIVERSARY, TV_SHOW_ANNIVERSARY ->
                    "💬 Qual momento desse título mais te marcou? Conta nos comentários!";
            case PERSON_BIRTHDAY ->
                    "💬 Qual é sua obra favorita? Deixa nos comentários!";
            case TRIVIA, BEHIND_THE_SCENES ->
                    "💬 Você já sabia disso? Manda nos comentários!";
            case RELEASE_REMINDER ->
                    "💬 Você está animado? Marca quem vai assistir junto!";
            case RECOMMENDATION ->
                    "💬 Já assistiu? Deixa sua avaliação nos comentários!";
            case COMPARISON ->
                    "💬 Com qual você fica? Vote nos comentários!";
        };
    }

    private List<String> buildInstagramHashtags(Content content) {
        List<String> hashtags = new ArrayList<>();

        // Base hashtags for all content
        hashtags.add("#cinema");
        hashtags.add("#filmes");
        hashtags.add("#entretenimento");
        hashtags.add("#cultura");

        // Type-specific hashtags
        switch (content.getType()) {
            case MOVIE_ANNIVERSARY -> {
                hashtags.add("#classicodocinema");
                hashtags.add("#aniversariodofilme");
                hashtags.add("#setearte");
            }
            case SERIES_ANNIVERSARY, TV_SHOW_ANNIVERSARY -> {
                hashtags.add("#series");
                hashtags.add("#seriesvicio");
                hashtags.add("#tv");
            }
            case PERSON_BIRTHDAY -> {
                hashtags.add("#ator");
                hashtags.add("#diretor");
                hashtags.add("#aniversario");
            }
            case TRIVIA -> {
                hashtags.add("#curiosidades");
                hashtags.add("#vocesabia");
                hashtags.add("#nerdices");
            }
            case BEHIND_THE_SCENES -> {
                hashtags.add("#bastidores");
                hashtags.add("#behindbthescenes");
                hashtags.add("#producao");
            }
            case RELEASE_REMINDER -> {
                hashtags.add("#lancamento");
                hashtags.add("#embreve");
                hashtags.add("#recomendacao");
            }
            case RECOMMENDATION -> {
                hashtags.add("#indicacao");
                hashtags.add("#assistaagora");
                hashtags.add("#deveassistir");
            }
            case COMPARISON -> {
                hashtags.add("#versus");
                hashtags.add("#debate");
                hashtags.add("#opiniao");
            }
        }

        // Add tags from content as hashtags
        if (content.getTags() != null) {
            content.getTags().stream()
                    .limit(3)
                    .map(t -> "#" + t.replace(" ", "").toLowerCase())
                    .forEach(hashtags::add);
        }

        // Instagram: 5–12 hashtags
        return hashtags.stream().distinct().limit(12).toList();
    }

    // ---- Utilities ----

    private int calculateYears(java.time.LocalDate eventDate) {
        if (eventDate == null) return 0;
        int currentYear = Year.now().getValue();
        return currentYear - eventDate.getYear();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}

package com.cineposts.service;

import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.exception.BusinessRuleException;
import com.cineposts.exception.UnauthorizedActionException;
import com.cineposts.model.PostSuggestion;
import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cineposts.repository.PostSuggestionRepository;
import com.cineposts.repository.UserRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostSuggestionImageService")
class PostSuggestionImageServiceTest {

    @Mock private PostSuggestionRepository suggestionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostSuggestionService postSuggestionService;

    @InjectMocks private PostSuggestionImageService imageService;

    private User ownerUser;
    private PostSuggestion suggestion;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id("user-001")
                .username("joao")
                .role(Role.USER)
                .build();

        suggestion = PostSuggestion.builder()
                .id("sug-001")
                .createdBy("user-001")
                .images(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("deve adicionar metadados da imagem na sugestao")
    void addImage_success_addsMetadata() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(ownerUser));
        when(suggestionRepository.findById("sug-001")).thenReturn(Optional.of(suggestion));
        when(suggestionRepository.save(any(PostSuggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postSuggestionService.toResponse(any(PostSuggestion.class))).thenAnswer(invocation -> {
            PostSuggestion saved = invocation.getArgument(0);
            return PostSuggestionResponse.builder()
                    .id(saved.getId())
                    .images(saved.getImages())
                    .build();
        });

        PostSuggestionResponse response = imageService.addImage("sug-001", file, "joao");

        assertThat(response.getImages()).hasSize(1);
        assertThat(response.getImages().getFirst().getFormat()).isEqualTo("png");
        assertThat(response.getImages().getFirst().getUrl()).startsWith("https://fake-storage.cineposts.dev/");
        assertThat(response.getImages().getFirst().getPublicId()).startsWith("cineposts/");
        assertThat(response.getImages().getFirst().getSizeKb()).isPositive();

        verify(suggestionRepository).save(any(PostSuggestion.class));
    }

    @Test
    @DisplayName("deve rejeitar formato de arquivo invalido")
    void addImage_invalidFormat_throwsBusinessRuleException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.gif",
                "image/gif",
                "fake-image-content".getBytes()
        );

        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(ownerUser));
        when(suggestionRepository.findById("sug-001")).thenReturn(Optional.of(suggestion));

        assertThatThrownBy(() -> imageService.addImage("sug-001", file, "joao"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Unsupported image format");
    }

    @Test
    @DisplayName("deve rejeitar arquivo acima de 5MB")
    void addImage_tooLarge_throwsBusinessRuleException() {
        byte[] payload = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.png",
                "image/png",
                payload
        );

        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(ownerUser));
        when(suggestionRepository.findById("sug-001")).thenReturn(Optional.of(suggestion));

        assertThatThrownBy(() -> imageService.addImage("sug-001", file, "joao"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5MB");
    }

    @Test
    @DisplayName("usuario sem permissao nao deve adicionar imagem")
    void addImage_nonOwner_throwsUnauthorizedActionException() {
        User otherUser = User.builder()
                .id("user-999")
                .username("maria")
                .role(Role.USER)
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "poster.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        when(userRepository.findByUsername("maria")).thenReturn(Optional.of(otherUser));
        when(suggestionRepository.findById("sug-001")).thenReturn(Optional.of(suggestion));

        assertThatThrownBy(() -> imageService.addImage("sug-001", file, "maria"))
                .isInstanceOf(UnauthorizedActionException.class);
    }
}

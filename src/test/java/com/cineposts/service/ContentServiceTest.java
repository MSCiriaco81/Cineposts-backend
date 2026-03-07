package com.cineposts.service;

import com.cineposts.dto.request.CreateContentRequest;
import com.cineposts.dto.request.UpdateContentRequest;
import com.cineposts.dto.response.ContentResponse;
import com.cineposts.exception.BusinessRuleException;
import com.cineposts.exception.ResourceNotFoundException;
import com.cineposts.exception.UnauthorizedActionException;
import com.cineposts.model.Content;
import com.cineposts.model.User;
import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.ContentRepository;
import com.cineposts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentService")
class ContentServiceTest {

    @Mock private ContentRepository contentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ContentService contentService;

    private User regularUser;
    private User adminUser;
    private Content pendingContent;
    private Content approvedContent;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id("user-001")
                .username("joao")
                .role(Role.USER)
                .active(true)
                .build();

        adminUser = User.builder()
                .id("admin-001")
                .username("admin")
                .role(Role.ADMIN)
                .active(true)
                .build();

        pendingContent = Content.builder()
                .id("content-001")
                .type(ContentType.MOVIE_ANNIVERSARY)
                .title("Pulp Fiction")
                .description("Clássico de Tarantino")
                .createdBy("user-001")
                .createdByUsername("joao")
                .status(ContentStatus.PENDING)
                .build();

        approvedContent = Content.builder()
                .id("content-002")
                .type(ContentType.TRIVIA)
                .title("Curiosidade sobre Matrix")
                .description("Fato interessante")
                .createdBy("user-001")
                .createdByUsername("joao")
                .status(ContentStatus.APPROVED)
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar conteúdo com status PENDING")
        void create_success_statusIsPending() {
            CreateContentRequest request = new CreateContentRequest();
            request.setType(ContentType.MOVIE_ANNIVERSARY);
            request.setTitle("Pulp Fiction");
            request.setDescription("Clássico de Tarantino");

            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.save(any(Content.class))).thenAnswer(inv -> {
                Content c = inv.getArgument(0);
                c.setId("new-content-id");
                return c;
            });

            ContentResponse response = contentService.create(request, "joao");

            assertThat(response.getStatus()).isEqualTo(ContentStatus.PENDING);
            assertThat(response.getCreatedBy()).isEqualTo("user-001");
            assertThat(response.getCreatedByUsername()).isEqualTo("joao");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("dono deve conseguir editar conteúdo PENDING")
        void update_ownerCanEditPendingContent() {
            UpdateContentRequest request = new UpdateContentRequest();
            request.setTitle("Pulp Fiction — 30 Anos");

            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-001")).thenReturn(Optional.of(pendingContent));
            when(contentRepository.save(any())).thenReturn(pendingContent);

            ContentResponse response = contentService.update("content-001", request, "joao");

            assertThat(pendingContent.getTitle()).isEqualTo("Pulp Fiction — 30 Anos");
        }

        @Test
        @DisplayName("usuário comum não deve editar conteúdo APPROVED diretamente")
        void update_regularUserCannotEditApprovedContent() {
            UpdateContentRequest request = new UpdateContentRequest();
            request.setTitle("Novo Título");

            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-002")).thenReturn(Optional.of(approvedContent));

            assertThatThrownBy(() -> contentService.update("content-002", request, "joao"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("EditRequest");
        }

        @Test
        @DisplayName("admin deve conseguir editar conteúdo APPROVED diretamente")
        void update_adminCanEditApprovedContent() {
            UpdateContentRequest request = new UpdateContentRequest();
            request.setTitle("Título Corrigido");

            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(contentRepository.findById("content-002")).thenReturn(Optional.of(approvedContent));
            when(contentRepository.save(any())).thenReturn(approvedContent);

            assertThatNoException().isThrownBy(() ->
                    contentService.update("content-002", request, "admin"));
        }

        @Test
        @DisplayName("usuário não deve editar conteúdo de outro usuário")
        void update_nonOwnerCannotEdit() {
            User otherUser = User.builder()
                    .id("user-999")
                    .username("outro")
                    .role(Role.USER)
                    .build();

            UpdateContentRequest request = new UpdateContentRequest();
            request.setTitle("Título Modificado");

            when(userRepository.findByUsername("outro")).thenReturn(Optional.of(otherUser));
            when(contentRepository.findById("content-001")).thenReturn(Optional.of(pendingContent));

            assertThatThrownBy(() -> contentService.update("content-001", request, "outro"))
                    .isInstanceOf(UnauthorizedActionException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando conteúdo não existe")
        void update_contentNotFound_throwsException() {
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contentService.update("invalid", new UpdateContentRequest(), "joao"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("dono deve conseguir deletar conteúdo PENDING")
        void delete_ownerCanDeletePendingContent() {
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-001")).thenReturn(Optional.of(pendingContent));

            assertThatNoException().isThrownBy(() ->
                    contentService.delete("content-001", "joao"));

            verify(contentRepository).deleteById("content-001");
        }

        @Test
        @DisplayName("usuário comum não deve deletar conteúdo APPROVED")
        void delete_regularUserCannotDeleteApprovedContent() {
            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-002")).thenReturn(Optional.of(approvedContent));

            assertThatThrownBy(() -> contentService.delete("content-002", "joao"))
                    .isInstanceOf(BusinessRuleException.class);

            verify(contentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("admin deve conseguir deletar conteúdo APPROVED")
        void delete_adminCanDeleteApprovedContent() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(contentRepository.findById("content-002")).thenReturn(Optional.of(approvedContent));

            assertThatNoException().isThrownBy(() ->
                    contentService.delete("content-002", "admin"));

            verify(contentRepository).deleteById("content-002");
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("deve retornar todos os conteúdos ordenados por data")
        void getAll_returnsAllContents() {
            when(contentRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(approvedContent, pendingContent));

            List<ContentResponse> result = contentService.getAll();

            assertThat(result).hasSize(2);
        }
    }
}

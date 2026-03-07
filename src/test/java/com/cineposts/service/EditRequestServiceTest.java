package com.cineposts.service;

import com.cineposts.dto.request.EditRequestPayload;
import com.cineposts.dto.response.EditRequestResponse;
import com.cineposts.exception.BusinessRuleException;
import com.cineposts.exception.ResourceNotFoundException;
import com.cineposts.model.Content;
import com.cineposts.model.EditRequest;
import com.cineposts.model.User;
import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import com.cineposts.model.enums.EditRequestStatus;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.ContentRepository;
import com.cineposts.repository.EditRequestRepository;
import com.cineposts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EditRequestService")
class EditRequestServiceTest {

    @Mock private EditRequestRepository editRequestRepository;
    @Mock private ContentRepository contentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private EditRequestService editRequestService;

    private User regularUser;
    private User adminUser;
    private Content approvedContent;
    private Content pendingContent;
    private EditRequest pendingEditRequest;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id("user-001").username("joao").role(Role.USER).active(true).build();

        adminUser = User.builder()
                .id("admin-001").username("admin").role(Role.ADMIN).active(true).build();

        approvedContent = Content.builder()
                .id("content-001").type(ContentType.MOVIE_ANNIVERSARY)
                .title("Matrix").description("Clássico de ficção científica")
                .createdBy("user-001").createdByUsername("joao")
                .status(ContentStatus.APPROVED).build();

        pendingContent = Content.builder()
                .id("content-002").type(ContentType.TRIVIA)
                .title("Curiosidade").description("Fato")
                .createdBy("user-001").createdByUsername("joao")
                .status(ContentStatus.PENDING).build();

        pendingEditRequest = EditRequest.builder()
                .id("req-001")
                .contentId("content-001")
                .requestedBy("user-001")
                .requestedByUsername("joao")
                .originalSnapshot(new HashMap<>())
                .proposedChanges(Map.of("title", "Matrix — 25 Anos"))
                .status(EditRequestStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("createEditRequest")
    class CreateEditRequest {

        @Test
        @DisplayName("deve criar solicitação para conteúdo APPROVED")
        void create_approvedContent_success() {
            EditRequestPayload payload = new EditRequestPayload();
            payload.setProposedChanges(Map.of("title", "Novo Título"));

            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-001")).thenReturn(Optional.of(approvedContent));
            when(editRequestRepository.save(any())).thenAnswer(inv -> {
                EditRequest r = inv.getArgument(0);
                r.setId("new-req-id");
                return r;
            });

            EditRequestResponse response = editRequestService.createEditRequest("content-001", payload, "joao");

            assertThat(response.getStatus()).isEqualTo(EditRequestStatus.PENDING);
            assertThat(response.getRequestedByUsername()).isEqualTo("joao");
            assertThat(response.getProposedChanges()).containsKey("title");
        }

        @Test
        @DisplayName("deve lançar exceção para conteúdo PENDING (não precisa de EditRequest)")
        void create_pendingContent_throwsException() {
            EditRequestPayload payload = new EditRequestPayload();
            payload.setProposedChanges(Map.of("title", "Novo Título"));

            when(userRepository.findByUsername("joao")).thenReturn(Optional.of(regularUser));
            when(contentRepository.findById("content-002")).thenReturn(Optional.of(pendingContent));

            assertThatThrownBy(() ->
                    editRequestService.createEditRequest("content-002", payload, "joao"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("PENDING");
        }
    }

    @Nested
    @DisplayName("approveEditRequest")
    class ApproveEditRequest {

        @Test
        @DisplayName("deve aprovar solicitação e aplicar mudanças no conteúdo")
        void approve_success_appliesChangesToContent() {
            pendingEditRequest.setProposedChanges(Map.of("title", "Matrix — 25 Anos"));

            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(editRequestRepository.findById("req-001")).thenReturn(Optional.of(pendingEditRequest));
            when(contentRepository.findById("content-001")).thenReturn(Optional.of(approvedContent));
            when(contentRepository.save(any())).thenReturn(approvedContent);
            when(editRequestRepository.save(any())).thenReturn(pendingEditRequest);

            EditRequestResponse response = editRequestService.approveEditRequest("req-001", "admin");

            assertThat(response.getStatus()).isEqualTo(EditRequestStatus.APPROVED);
            assertThat(approvedContent.getTitle()).isEqualTo("Matrix — 25 Anos");
            assertThat(response.getReviewedBy()).isEqualTo("admin-001");
        }

        @Test
        @DisplayName("deve lançar exceção ao tentar aprovar solicitação já processada")
        void approve_alreadyProcessed_throwsException() {
            pendingEditRequest.setStatus(EditRequestStatus.APPROVED);

            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(editRequestRepository.findById("req-001")).thenReturn(Optional.of(pendingEditRequest));

            assertThatThrownBy(() ->
                    editRequestService.approveEditRequest("req-001", "admin"))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    @DisplayName("rejectEditRequest")
    class RejectEditRequest {

        @Test
        @DisplayName("deve rejeitar solicitação sem alterar o conteúdo")
        void reject_success_doesNotModifyContent() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(editRequestRepository.findById("req-001")).thenReturn(Optional.of(pendingEditRequest));
            when(editRequestRepository.save(any())).thenReturn(pendingEditRequest);

            EditRequestResponse response = editRequestService.rejectEditRequest("req-001", "admin");

            assertThat(response.getStatus()).isEqualTo(EditRequestStatus.REJECTED);
            verify(contentRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando solicitação não existe")
        void reject_notFound_throwsException() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(editRequestRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    editRequestService.rejectEditRequest("invalid", "admin"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

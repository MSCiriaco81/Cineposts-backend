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
import com.cineposts.repository.ContentRepository;
import com.cineposts.repository.EditRequestRepository;
import com.cineposts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EditRequestService {

    private final EditRequestRepository editRequestRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    public EditRequestResponse createEditRequest(String contentId,
                                                 EditRequestPayload payload,
                                                 String username) {
        User user = loadUser(username);
        Content content = findContent(contentId);

        // Only APPROVED content requires the edit request workflow.
        // PENDING content can be directly edited by owner.
        if (content.getStatus() != ContentStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Edit requests are only required for APPROVED content. " +
                    "PENDING content can be directly edited.");
        }

        Map<String, Object> snapshot = contentToSnapshot(content);

        EditRequest editRequest = EditRequest.builder()
                .contentId(contentId)
                .requestedBy(user.getId())
                .requestedByUsername(user.getUsername())
                .originalSnapshot(snapshot)
                .proposedChanges(payload.getProposedChanges())
                .status(EditRequestStatus.PENDING)
                .build();

        return toResponse(editRequestRepository.save(editRequest));
    }

    public List<EditRequestResponse> getAllEditRequests() {
        return editRequestRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EditRequestResponse approveEditRequest(String requestId, String adminUsername) {
        User admin = loadUser(adminUsername);
        EditRequest editRequest = findEditRequest(requestId);

        validatePending(editRequest);

        // Apply proposed changes to the actual content document
        Content content = findContent(editRequest.getContentId());
        applyProposedChanges(content, editRequest.getProposedChanges());
        contentRepository.save(content);

        editRequest.setStatus(EditRequestStatus.APPROVED);
        editRequest.setReviewedBy(admin.getId());
        editRequest.setReviewedAt(LocalDateTime.now());

        return toResponse(editRequestRepository.save(editRequest));
    }

    public EditRequestResponse rejectEditRequest(String requestId, String adminUsername) {
        User admin = loadUser(adminUsername);
        EditRequest editRequest = findEditRequest(requestId);

        validatePending(editRequest);

        editRequest.setStatus(EditRequestStatus.REJECTED);
        editRequest.setReviewedBy(admin.getId());
        editRequest.setReviewedAt(LocalDateTime.now());

        return toResponse(editRequestRepository.save(editRequest));
    }

    /**
     * Applies only the fields present in proposedChanges to the Content document.
     * Uses string key matching to safely apply partial updates from the Map.
     */
    private void applyProposedChanges(Content content, Map<String, Object> changes) {
        changes.forEach((key, value) -> {
            switch (key) {
                case "title" -> content.setTitle((String) value);
                case "description" -> content.setDescription((String) value);
                case "relatedTitle" -> content.setRelatedTitle((String) value);
                case "relatedPerson" -> content.setRelatedPerson((String) value);
                case "eventDate" -> {
                    if (value instanceof String s) content.setEventDate(LocalDate.parse(s));
                }
                case "type" -> {
                    if (value instanceof String s) content.setType(ContentType.valueOf(s));
                }
                case "tags" -> {
                    if (value instanceof List<?> list) {
                        content.setTags(list.stream().map(Object::toString).collect(Collectors.toList()));
                    }
                }
                default -> {
                    // Unknown fields are silently ignored — protects against injection of system fields
                }
            }
        });
    }

    private Map<String, Object> contentToSnapshot(Content content) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", content.getId());
        snapshot.put("type", content.getType() != null ? content.getType().name() : null);
        snapshot.put("title", content.getTitle());
        snapshot.put("description", content.getDescription());
        snapshot.put("relatedTitle", content.getRelatedTitle());
        snapshot.put("relatedPerson", content.getRelatedPerson());
        snapshot.put("eventDate", content.getEventDate() != null ? content.getEventDate().toString() : null);
        snapshot.put("tags", content.getTags());
        snapshot.put("status", content.getStatus() != null ? content.getStatus().name() : null);
        snapshot.put("createdAt", content.getCreatedAt() != null ? content.getCreatedAt().toString() : null);
        return snapshot;
    }

    private void validatePending(EditRequest editRequest) {
        if (editRequest.getStatus() != EditRequestStatus.PENDING) {
            throw new BusinessRuleException(
                    "Edit request has already been " + editRequest.getStatus().name().toLowerCase());
        }
    }

    private EditRequest findEditRequest(String id) {
        return editRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Edit request not found: " + id));
    }

    private Content findContent(String id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public EditRequestResponse toResponse(EditRequest r) {
        return EditRequestResponse.builder()
                .id(r.getId())
                .contentId(r.getContentId())
                .requestedBy(r.getRequestedBy())
                .requestedByUsername(r.getRequestedByUsername())
                .originalSnapshot(r.getOriginalSnapshot())
                .proposedChanges(r.getProposedChanges())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .reviewedBy(r.getReviewedBy())
                .reviewedAt(r.getReviewedAt())
                .build();
    }
}

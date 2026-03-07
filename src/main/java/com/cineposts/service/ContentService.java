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
import com.cineposts.model.enums.Role;
import com.cineposts.repository.ContentRepository;
import com.cineposts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    public ContentResponse create(CreateContentRequest request, String username) {
        User user = loadUser(username);

        Content content = Content.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .relatedTitle(request.getRelatedTitle())
                .relatedPerson(request.getRelatedPerson())
                .eventDate(request.getEventDate())
                .tags(request.getTags())
                .createdBy(user.getId())
                .createdByUsername(user.getUsername())
                .status(ContentStatus.PENDING)
                .build();

        return toResponse(contentRepository.save(content));
    }

    public List<ContentResponse> getAll() {
        return contentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContentResponse getById(String id) {
        return toResponse(findContent(id));
    }

    public ContentResponse update(String id, UpdateContentRequest request, String username) {
        User user = loadUser(username);
        Content content = findContent(id);

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = content.getCreatedBy().equals(user.getId());

        // Business rule: APPROVED content cannot be directly edited by regular users
        if (!isAdmin && content.getStatus() == ContentStatus.APPROVED) {
            throw new BusinessRuleException(
                    "Cannot directly edit approved content. Please submit an EditRequest instead."
            );
        }

        // Business rule: only owner or admin can edit
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("You can only edit your own content");
        }

        if (request.getType() != null) content.setType(request.getType());
        if (request.getTitle() != null) content.setTitle(request.getTitle());
        if (request.getDescription() != null) content.setDescription(request.getDescription());
        if (request.getRelatedTitle() != null) content.setRelatedTitle(request.getRelatedTitle());
        if (request.getRelatedPerson() != null) content.setRelatedPerson(request.getRelatedPerson());
        if (request.getEventDate() != null) content.setEventDate(request.getEventDate());
        if (request.getTags() != null) content.setTags(request.getTags());

        // Only admins can change status directly
        if (isAdmin && request.getStatus() != null) {
            content.setStatus(request.getStatus());
        }

        return toResponse(contentRepository.save(content));
    }

    public void delete(String id, String username) {
        User user = loadUser(username);
        Content content = findContent(id);

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = content.getCreatedBy().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("You can only delete your own content");
        }

        if (!isAdmin && content.getStatus() == ContentStatus.APPROVED) {
            throw new BusinessRuleException("Approved content cannot be deleted by non-admins");
        }

        contentRepository.deleteById(id);
    }

    public Content findContent(String id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public ContentResponse toResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .type(content.getType())
                .title(content.getTitle())
                .description(content.getDescription())
                .relatedTitle(content.getRelatedTitle())
                .relatedPerson(content.getRelatedPerson())
                .eventDate(content.getEventDate())
                .tags(content.getTags())
                .createdBy(content.getCreatedBy())
                .createdByUsername(content.getCreatedByUsername())
                .status(content.getStatus())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
}

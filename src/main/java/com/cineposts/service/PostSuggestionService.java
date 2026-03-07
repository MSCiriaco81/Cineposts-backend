package com.cineposts.service;

import com.cineposts.dto.request.UpdateSuggestionRequest;
import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.exception.ResourceNotFoundException;
import com.cineposts.exception.UnauthorizedActionException;
import com.cineposts.model.Content;
import com.cineposts.model.PostSuggestion;
import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.PostSuggestionRepository;
import com.cineposts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSuggestionService {

    private final PostSuggestionRepository suggestionRepository;
    private final PostGeneratorService generatorService;
    private final ContentService contentService;
    private final UserRepository userRepository;

    public PostSuggestionResponse generateTwitter(String contentId, String username) {
        Content content = contentService.findContent(contentId);
        User user = loadUser(username);

        PostSuggestion suggestion = generatorService.generateTwitterPost(
                content, user.getId(), user.getUsername());

        return toResponse(suggestionRepository.save(suggestion));
    }

    public PostSuggestionResponse generateInstagram(String contentId, String username) {
        Content content = contentService.findContent(contentId);
        User user = loadUser(username);

        PostSuggestion suggestion = generatorService.generateInstagramPost(
                content, user.getId(), user.getUsername());

        return toResponse(suggestionRepository.save(suggestion));
    }

    public List<PostSuggestionResponse> getAll() {
        return suggestionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PostSuggestionResponse getById(String id) {
        return toResponse(findSuggestion(id));
    }

    public PostSuggestionResponse update(String id, UpdateSuggestionRequest request, String username) {
        User user = loadUser(username);
        PostSuggestion suggestion = findSuggestion(id);

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = suggestion.getCreatedBy().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("You can only edit your own suggestions");
        }

        if (request.getHook() != null) suggestion.setHook(request.getHook());
        if (request.getCaption() != null) suggestion.setCaption(request.getCaption());
        if (request.getHashtags() != null) suggestion.setHashtags(request.getHashtags());
        if (request.getCta() != null) suggestion.setCta(request.getCta());
        if (request.getStatus() != null) suggestion.setStatus(request.getStatus());

        return toResponse(suggestionRepository.save(suggestion));
    }

    private PostSuggestion findSuggestion(String id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public PostSuggestionResponse toResponse(PostSuggestion s) {
        return PostSuggestionResponse.builder()
                .id(s.getId())
                .contentId(s.getContentId())
                .platform(s.getPlatform())
                .hook(s.getHook())
                .caption(s.getCaption())
                .hashtags(s.getHashtags())
                .cta(s.getCta())
                .createdBy(s.getCreatedBy())
                .createdByUsername(s.getCreatedByUsername())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}

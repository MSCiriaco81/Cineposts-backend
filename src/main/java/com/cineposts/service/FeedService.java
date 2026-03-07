package com.cineposts.service;

import com.cineposts.dto.response.ContentResponse;
import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.repository.ContentRepository;
import com.cineposts.repository.PostSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FeedService provides team-wide visibility into all content and suggestions.
 * Any authenticated user can view the collaborative feed.
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    private final ContentRepository contentRepository;
    private final PostSuggestionRepository suggestionRepository;
    private final ContentService contentService;
    private final PostSuggestionService suggestionService;

    public List<ContentResponse> getTeamContentFeed() {
        return contentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(contentService::toResponse)
                .collect(Collectors.toList());
    }

    public List<PostSuggestionResponse> getTeamSuggestionFeed() {
        return suggestionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(suggestionService::toResponse)
                .collect(Collectors.toList());
    }
}

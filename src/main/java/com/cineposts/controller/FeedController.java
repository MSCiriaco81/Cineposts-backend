package com.cineposts.controller;

import com.cineposts.dto.response.ContentResponse;
import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@Tag(name = "Team Feed", description = "Collaborative feed — see all content and suggestions created by the team")
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/contents")
    @Operation(summary = "View all team content (most recent first)")
    public ResponseEntity<List<ContentResponse>> getTeamContentFeed() {
        return ResponseEntity.ok(feedService.getTeamContentFeed());
    }

    @GetMapping("/post-suggestions")
    @Operation(summary = "View all team post suggestions (most recent first)")
    public ResponseEntity<List<PostSuggestionResponse>> getTeamSuggestionFeed() {
        return ResponseEntity.ok(feedService.getTeamSuggestionFeed());
    }
}

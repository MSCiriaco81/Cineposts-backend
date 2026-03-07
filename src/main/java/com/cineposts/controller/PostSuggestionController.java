package com.cineposts.controller;

import com.cineposts.dto.request.UpdateSuggestionRequest;
import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.service.PostSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Post Suggestions", description = "Generate and manage social media post suggestions")
public class PostSuggestionController {

    private final PostSuggestionService suggestionService;

    @PostMapping("/contents/{id}/suggestions/twitter")
    @Operation(summary = "Generate a Twitter post suggestion from content")
    public ResponseEntity<PostSuggestionResponse> generateTwitter(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suggestionService.generateTwitter(id, userDetails.getUsername()));
    }

    @PostMapping("/contents/{id}/suggestions/instagram")
    @Operation(summary = "Generate an Instagram post suggestion from content")
    public ResponseEntity<PostSuggestionResponse> generateInstagram(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suggestionService.generateInstagram(id, userDetails.getUsername()));
    }

    @GetMapping("/post-suggestions")
    @Operation(summary = "List all post suggestions")
    public ResponseEntity<List<PostSuggestionResponse>> getAll() {
        return ResponseEntity.ok(suggestionService.getAll());
    }

    @GetMapping("/post-suggestions/{id}")
    @Operation(summary = "Get a post suggestion by ID")
    public ResponseEntity<PostSuggestionResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(suggestionService.getById(id));
    }

    @PutMapping("/post-suggestions/{id}")
    @Operation(summary = "Edit a post suggestion (owner or admin only)")
    public ResponseEntity<PostSuggestionResponse> update(
            @PathVariable String id,
            @RequestBody UpdateSuggestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(suggestionService.update(id, request, userDetails.getUsername()));
    }
}

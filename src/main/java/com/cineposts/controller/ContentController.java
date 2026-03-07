package com.cineposts.controller;

import com.cineposts.dto.request.CreateContentRequest;
import com.cineposts.dto.request.UpdateContentRequest;
import com.cineposts.dto.response.ContentResponse;
import com.cineposts.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Editorial content management")
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    @Operation(summary = "Create new editorial content")
    public ResponseEntity<ContentResponse> create(
            @Valid @RequestBody CreateContentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.create(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "List all content")
    public ResponseEntity<List<ContentResponse>> getAll() {
        return ResponseEntity.ok(contentService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get content by ID")
    public ResponseEntity<ContentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(contentService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update content (owner or admin only; approved content requires EditRequest for non-admins)")
    public ResponseEntity<ContentResponse> update(
            @PathVariable String id,
            @RequestBody UpdateContentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(contentService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete content (owner or admin only)")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        contentService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

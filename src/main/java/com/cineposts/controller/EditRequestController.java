package com.cineposts.controller;

import com.cineposts.dto.request.EditRequestPayload;
import com.cineposts.dto.response.EditRequestResponse;
import com.cineposts.service.EditRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
@Tag(name = "Edit Requests", description = "Request changes to approved content — requires admin review")
public class EditRequestController {

    private final EditRequestService editRequestService;

    @PostMapping("/{id}/edit-request")
    @Operation(summary = "Submit an edit request for approved content")
    public ResponseEntity<EditRequestResponse> createEditRequest(
            @PathVariable String id,
            @Valid @RequestBody EditRequestPayload payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(editRequestService.createEditRequest(id, payload, userDetails.getUsername()));
    }
}

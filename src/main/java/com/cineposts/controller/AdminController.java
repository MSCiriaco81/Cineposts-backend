package com.cineposts.controller;

import com.cineposts.dto.request.CreateUserRequest;
import com.cineposts.dto.request.UpdateUserRequest;
import com.cineposts.dto.response.EditRequestResponse;
import com.cineposts.dto.response.UserResponse;
import com.cineposts.service.AdminService;
import com.cineposts.service.EditRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only operations: user management and edit request review")
public class AdminController {

    private final AdminService adminService;
    private final EditRequestService editRequestService;

    // ---- User Management ----

    @PostMapping("/users")
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                    @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Deactivate a user (soft delete)")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Edit Request Review ----

    @GetMapping("/edit-requests")
    @Operation(summary = "List all edit requests")
    public ResponseEntity<List<EditRequestResponse>> getAllEditRequests() {
        return ResponseEntity.ok(editRequestService.getAllEditRequests());
    }

    @PostMapping("/edit-requests/{id}/approve")
    @Operation(summary = "Approve an edit request and apply changes to content")
    public ResponseEntity<EditRequestResponse> approveEditRequest(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(editRequestService.approveEditRequest(id, userDetails.getUsername()));
    }

    @PostMapping("/edit-requests/{id}/reject")
    @Operation(summary = "Reject an edit request")
    public ResponseEntity<EditRequestResponse> rejectEditRequest(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(editRequestService.rejectEditRequest(id, userDetails.getUsername()));
    }
}

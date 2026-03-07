package com.cineposts.dto.response;

import com.cineposts.model.enums.EditRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditRequestResponse {
    private String id;
    private String contentId;
    private String requestedBy;
    private String requestedByUsername;
    private Map<String, Object> originalSnapshot;
    private Map<String, Object> proposedChanges;
    private EditRequestStatus status;
    private LocalDateTime createdAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}

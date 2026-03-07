package com.cineposts.model;

import com.cineposts.model.enums.EditRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "edit_requests")
public class EditRequest {

    @Id
    private String id;

    private String contentId;

    private String requestedBy;

    private String requestedByUsername;

    /**
     * Full snapshot of the Content document at the time this request was created.
     * Stored as a Map to allow flexible schema capture without circular dependencies.
     */
    private Map<String, Object> originalSnapshot;

    /**
     * The fields the requester wants to change. Only changed fields are included.
     */
    private Map<String, Object> proposedChanges;

    @Builder.Default
    private EditRequestStatus status = EditRequestStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    private String reviewedBy;

    private LocalDateTime reviewedAt;
}

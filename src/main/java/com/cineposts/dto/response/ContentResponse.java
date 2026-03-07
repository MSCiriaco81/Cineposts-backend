package com.cineposts.dto.response;

import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    private String id;
    private ContentType type;
    private String title;
    private String description;
    private String relatedTitle;
    private String relatedPerson;
    private LocalDate eventDate;
    private List<String> tags;
    private String createdBy;
    private String createdByUsername;
    private ContentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.cineposts.dto.response;

import com.cineposts.model.enums.Platform;
import com.cineposts.model.enums.SuggestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSuggestionResponse {
    private String id;
    private String contentId;
    private Platform platform;
    private String hook;
    private String caption;
    private List<String> hashtags;
    private String cta;
    private String createdBy;
    private String createdByUsername;
    private SuggestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

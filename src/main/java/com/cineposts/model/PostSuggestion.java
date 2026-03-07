package com.cineposts.model;

import com.cineposts.model.enums.Platform;
import com.cineposts.model.enums.SuggestionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post_suggestions")
public class PostSuggestion {

    @Id
    private String id;

    private String contentId;

    private Platform platform;

    private String hook;

    private String caption;

    private List<String> hashtags;

    private String cta;

    private String createdBy;

    private String createdByUsername;

    @Builder.Default
    private SuggestionStatus status = SuggestionStatus.DRAFT;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

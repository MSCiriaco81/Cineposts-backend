package com.cineposts.model;

import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "contents")
public class Content {

    @Id
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

    @Builder.Default
    private ContentStatus status = ContentStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

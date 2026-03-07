package com.cineposts.dto.request;

import com.cineposts.model.enums.ContentStatus;
import com.cineposts.model.enums.ContentType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateContentRequest {

    private ContentType type;

    private String title;

    private String description;

    private String relatedTitle;

    private String relatedPerson;

    private LocalDate eventDate;

    private List<String> tags;

    // Only ADMIN can change status directly via update
    private ContentStatus status;
}

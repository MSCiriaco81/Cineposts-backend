package com.cineposts.dto.request;

import com.cineposts.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateContentRequest {

    @NotNull(message = "Content type is required")
    private ContentType type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String relatedTitle;

    private String relatedPerson;

    private LocalDate eventDate;

    private List<String> tags;
}

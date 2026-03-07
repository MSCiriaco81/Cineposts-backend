package com.cineposts.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class EditRequestPayload {

    @NotEmpty(message = "Proposed changes cannot be empty")
    private Map<String, Object> proposedChanges;
}

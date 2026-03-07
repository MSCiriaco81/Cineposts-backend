package com.cineposts.dto.request;

import com.cineposts.model.enums.SuggestionStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSuggestionRequest {

    private String hook;

    private String caption;

    private List<String> hashtags;

    private String cta;

    private SuggestionStatus status;
}

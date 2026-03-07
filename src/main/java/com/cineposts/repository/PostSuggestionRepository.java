package com.cineposts.repository;

import com.cineposts.model.PostSuggestion;
import com.cineposts.model.enums.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostSuggestionRepository extends MongoRepository<PostSuggestion, String> {

    List<PostSuggestion> findByContentId(String contentId);

    List<PostSuggestion> findByCreatedBy(String createdBy);

    List<PostSuggestion> findByContentIdAndPlatform(String contentId, Platform platform);

    List<PostSuggestion> findAllByOrderByCreatedAtDesc();
}

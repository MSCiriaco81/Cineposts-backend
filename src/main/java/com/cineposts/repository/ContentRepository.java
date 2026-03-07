package com.cineposts.repository;

import com.cineposts.model.Content;
import com.cineposts.model.enums.ContentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContentRepository extends MongoRepository<Content, String> {

    List<Content> findByCreatedBy(String createdBy);

    List<Content> findByStatus(ContentStatus status);

    List<Content> findAllByOrderByCreatedAtDesc();
}

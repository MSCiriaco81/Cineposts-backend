package com.cineposts.repository;

import com.cineposts.model.EditRequest;
import com.cineposts.model.enums.EditRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EditRequestRepository extends MongoRepository<EditRequest, String> {

    List<EditRequest> findByContentId(String contentId);

    List<EditRequest> findByStatus(EditRequestStatus status);

    List<EditRequest> findByRequestedBy(String requestedBy);
}

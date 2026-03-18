package com.cineposts.service;

import com.cineposts.dto.response.PostSuggestionResponse;
import com.cineposts.exception.BusinessRuleException;
import com.cineposts.exception.ResourceNotFoundException;
import com.cineposts.exception.UnauthorizedActionException;
import com.cineposts.model.ImageAsset;
import com.cineposts.model.PostSuggestion;
import com.cineposts.model.User;
import com.cineposts.model.enums.Role;
import com.cineposts.repository.PostSuggestionRepository;
import com.cineposts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostSuggestionImageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> CONTENT_TYPE_TO_FORMAT = Map.of(
            "image/jpeg", "jpeg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final PostSuggestionRepository suggestionRepository;
    private final UserRepository userRepository;
    private final PostSuggestionService postSuggestionService;

    public PostSuggestionResponse addImage(String suggestionId, MultipartFile file, String username) {
        User user = loadUser(username);
        PostSuggestion suggestion = findSuggestion(suggestionId);

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = suggestion.getCreatedBy().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("You can only edit your own suggestions");
        }

        ImageAsset imageAsset = validateAndSimulateUpload(file);

        if (suggestion.getImages() == null) {
            suggestion.setImages(new ArrayList<>());
        }
        suggestion.getImages().add(imageAsset);

        PostSuggestion savedSuggestion = suggestionRepository.save(suggestion);
        return postSuggestionService.toResponse(savedSuggestion);
    }

    private ImageAsset validateAndSimulateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Image file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Image size exceeds max allowed limit of 5MB");
        }

        String format = resolveFormat(file);
        if (format == null) {
            throw new BusinessRuleException("Unsupported image format. Allowed: jpg, jpeg, png, webp");
        }

        return simulateProcessingAndUpload(file, format);
    }

    private String resolveFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            String mapped = CONTENT_TYPE_TO_FORMAT.get(contentType.toLowerCase(Locale.ROOT));
            if (mapped != null) {
                return mapped;
            }
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            return null;
        }

        String extension = originalName.substring(originalName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "jpg", "jpeg", "png", "webp" -> extension;
            default -> null;
        };
    }

    private ImageAsset simulateProcessingAndUpload(MultipartFile file, String format) {
        long originalSize = file.getSize();

        // Simulates a compression and resize pipeline before upload.
        double compressionRate = originalSize > 1024 * 1024 ? 0.65 : 0.80;
        long processedSizeBytes = Math.max((long) (originalSize * compressionRate), 8 * 1024L);

        int width = originalSize > 1024 * 1024 ? 1280 : 1080;
        int height = originalSize > 1024 * 1024 ? 720 : 1080;

        String publicId = "cineposts/" + UUID.randomUUID().toString().replace("-", "");
        String url = "https://fake-storage.cineposts.dev/" + publicId + "." + format;

        double sizeKb = BigDecimal.valueOf(processedSizeBytes / 1024.0)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return ImageAsset.builder()
                .url(url)
                .publicId(publicId)
                .format(format)
                .width(width)
                .height(height)
                .sizeKb(sizeKb)
                .build();
    }

    private PostSuggestion findSuggestion(String id) {
        return suggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

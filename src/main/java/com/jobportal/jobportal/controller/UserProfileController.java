package com.jobportal.jobportal.controller;

import com.jobportal.jobportal.entity.UserProfile;
import com.jobportal.jobportal.repository.UserProfileRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    public UserProfileController(
            UserProfileRepository userProfileRepository) {

        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping
    public UserProfile createProfile(
            @RequestBody UserProfile profile) {

        return userProfileRepository.save(profile);
    }

    @GetMapping("/{userId}")
    public UserProfile getProfile(
            @PathVariable Long userId) {

        return userProfileRepository
                .findByUserId(userId)
                .orElse(null);
    }

    @PutMapping("/{userId}")
    public UserProfile updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfile profile) {

        UserProfile existingProfile =
                userProfileRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (existingProfile == null) {

            profile.setUserId(userId);

            return userProfileRepository.save(profile);
        }

        existingProfile.setPhone(
                profile.getPhone()
        );

        existingProfile.setEducation(
                profile.getEducation()
        );

        existingProfile.setSkills(
                profile.getSkills()
        );

        return userProfileRepository.save(existingProfile);
    }

    @PutMapping("/{userId}/resume")
    public UserProfile uploadResume(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        UserProfile profile =
                userProfileRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (profile == null) {
            return null;
        }

        Path uploadPath =
                Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName =
                file.getOriginalFilename();

        String fileName =
                UUID.randomUUID()
                        + "_"
                        + originalFileName;

        Path filePath =
                uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath
        );

        profile.setResumeFileName(
                originalFileName
        );

        profile.setResumePath(
                filePath.toString()
        );

        return userProfileRepository.save(profile);
    }

    @GetMapping("/{userId}/resume")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long userId)
            throws IOException {

        UserProfile profile =
                userProfileRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (profile == null ||
                profile.getResumePath() == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        Path filePath =
                Paths.get(
                        profile.getResumePath()
                );

        Resource resource =
                new UrlResource(
                        filePath.toUri()
                );

        if (!resource.exists()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                profile.getResumeFileName() +
                                "\""
                )
                .body(resource);
    }
}
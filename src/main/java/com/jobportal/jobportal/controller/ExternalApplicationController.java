package com.jobportal.jobportal.controller;

import com.jobportal.jobportal.entity.ExternalApplication;
import com.jobportal.jobportal.entity.User;
import com.jobportal.jobportal.repository.ExternalApplicationRepository;
import com.jobportal.jobportal.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/external-applications")
@CrossOrigin
public class ExternalApplicationController {

    private final ExternalApplicationRepository externalApplicationRepository;
    private final UserRepository userRepository;

    public ExternalApplicationController(
            ExternalApplicationRepository externalApplicationRepository,
            UserRepository userRepository) {

        this.externalApplicationRepository =
                externalApplicationRepository;

        this.userRepository = userRepository;
    }

    @PostMapping("/click")
    public ExternalApplication recordApplyClick(
            @RequestBody ExternalApplication application,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return null;
        }

        application.setUserId(user.getId());
        application.setStatus("APPLY_CLICKED");
        application.setCreatedAt(LocalDateTime.now());

        return externalApplicationRepository.save(application);
    }

    @GetMapping("/user/{userId}")
    public List<ExternalApplication> getUserExternalApplications(
            @PathVariable Long userId) {

        return externalApplicationRepository
                .findByUserId(userId);
    }

    @GetMapping("/company/{company}")
    public List<ExternalApplication> getCompanyApplications(
            @PathVariable String company) {

        return externalApplicationRepository
                .findByCompany(company);
    }

    @GetMapping("/job/{jobId}")
    public List<ExternalApplication> getJobApplications(
            @PathVariable String jobId) {

        return externalApplicationRepository
                .findByJobId(jobId);
    }

    @GetMapping
    public List<ExternalApplication> getAllExternalApplications() {

        return externalApplicationRepository.findAll();
    }
}
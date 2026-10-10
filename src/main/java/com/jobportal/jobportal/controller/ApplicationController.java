package com.jobportal.jobportal.controller;

import com.jobportal.jobportal.entity.Application;
import com.jobportal.jobportal.entity.User;
import com.jobportal.jobportal.repository.ApplicationRepository;
import com.jobportal.jobportal.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin
public class ApplicationController { 

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationController(
            ApplicationRepository applicationRepository,
            UserRepository userRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }


    // APPLY FOR A JOB

    @PostMapping
    public Application apply(
            @RequestBody Application application,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return null;
        }

        application.setUserId(user.getId());

        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }


    // GET USER APPLICATIONS

    @GetMapping("/user/{userId}")
    public List<Application> getUserApplications(
            @PathVariable Long userId) {

        return applicationRepository.findByUserId(userId);
    }


    // GET APPLICATIONS FOR A JOB

    @GetMapping("/job/{jobId}")
    public List<Application> getJobApplications(
            @PathVariable Long jobId) {

        return applicationRepository.findByJobId(jobId);
    }


    // UPDATE APPLICATION STATUS

    @PutMapping("/{id}/status")
    public Application updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Application application =
                applicationRepository
                        .findById(id)
                        .orElse(null);

        if (application == null) {
            return null;
        }

        application.setStatus(status);

        return applicationRepository.save(application);
    }
}
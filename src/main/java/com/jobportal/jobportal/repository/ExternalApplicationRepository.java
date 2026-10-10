package com.jobportal.jobportal.repository;

import com.jobportal.jobportal.entity.ExternalApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExternalApplicationRepository
        extends JpaRepository<ExternalApplication, Long> {

    List<ExternalApplication> findByUserId(Long userId);

    List<ExternalApplication> findByJobId(String jobId);

    List<ExternalApplication> findByCompany(String company);
}
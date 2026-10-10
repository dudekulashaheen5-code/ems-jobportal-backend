package com.jobportal.jobportal.controller;

import com.jobportal.jobportal.entity.CompanyJob;
import com.jobportal.jobportal.service.CompanyJobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-jobs")
@CrossOrigin
public class CompanyJobController {

    private final CompanyJobService companyJobService;

    public CompanyJobController(
            CompanyJobService companyJobService) {

        this.companyJobService = companyJobService;
    }

    @GetMapping("/{company}")
    public List<CompanyJob> getCompanyJobs(
            @PathVariable String company) {

        return companyJobService.getCompanyJobs(company);
    }
}
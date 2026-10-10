package com.jobportal.jobportal.entity;

public class CompanyJob {

    private String company;
    private String title;
    private String location;
    private String type;
    private String description;
    private String jobId;
    private String jobUrl;
    private String postedDate;

    public CompanyJob() {
    }

    public CompanyJob(
            String company,
            String title,
            String location,
            String type,
            String description,
            String jobId,
            String jobUrl,
            String postedDate) {

        this.company = company;
        this.title = title;
        this.location = location;
        this.type = type;
        this.description = description;
        this.jobId = jobId;
        this.jobUrl = jobUrl;
        this.postedDate = postedDate;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }
}
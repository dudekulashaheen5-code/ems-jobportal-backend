package com.jobportal.jobportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.jobportal.entity.CompanyJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompanyJobService {

    private static final int AMAZON_LIMIT = 50;
    private static final int OTHER_COMPANY_LIMIT = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CompanyJobService() {
        httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        objectMapper = new ObjectMapper();
    }

    public List<CompanyJob> getCompanyJobs(String company) {

        if (company == null || company.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String name = company.trim().toLowerCase();

        try {

            switch (name) {

                case "amazon":
                    return getAmazonJobs();

                case "microsoft":
                    return getMicrosoftJobs();

                case "google":
                    return getGoogleJobs();

                case "cognizant":
                    return getCognizantJobs();

                case "infosys":
                    return getInfosysJobs();

                default:
                    return new ArrayList<>();
            }

        } catch (Exception e) {

            System.out.println(
                    "Company job error: " + e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    // =========================================================
    // AMAZON - 50 JOBS
    // =========================================================

    private List<CompanyJob> getAmazonJobs() {

        List<CompanyJob> jobs = new ArrayList<>();

        try {

            int offset = 0;

            while (jobs.size() < AMAZON_LIMIT && offset < 500) {

                int remaining =
                        AMAZON_LIMIT - jobs.size();

                int limit =
                        Math.min(10, remaining);

                String url =
                        "https://www.amazon.jobs/en/search.json"
                                + "?normalized_location=Hyderabad"
                                + "&city=Hyderabad"
                                + "&country=IND"
                                + "&region=Telangana"
                                + "&county="
                                + "&query_options="
                                + "&result_limit=" + limit
                                + "&offset=" + offset;

                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header(
                                        "User-Agent",
                                        "Mozilla/5.0"
                                )
                                .header(
                                        "Accept",
                                        "application/json"
                                )
                                .GET()
                                .build();

                HttpResponse<String> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                        );

                if (response.statusCode() != 200) {
                    break;
                }

                JsonNode root =
                        objectMapper.readTree(
                                response.body()
                        );

                JsonNode jobArray =
                        root.get("jobs");

                if (jobArray == null ||
                        !jobArray.isArray() ||
                        jobArray.size() == 0) {
                    break;
                }

                for (JsonNode item : jobArray) {

                    if (jobs.size() >= AMAZON_LIMIT) {
                        break;
                    }

                    String title =
                            text(item, "title");

                    String location =
                            text(item, "location");

                    String id =
                            text(item, "id");

                    String description =
                            text(item, "description");

                    String posted =
                            text(item, "posted_date");

                    if (title.isBlank() ||
                            id.isBlank()) {
                        continue;
                    }

                    String jobUrl =
                            "https://www.amazon.jobs/en/jobs/"
                                    + id;

                    jobs.add(
                            new CompanyJob(
                                    "Amazon",
                                    title,
                                    location,
                                    "Full Time",
                                    clean(description),
                                    id,
                                    jobUrl,
                                    posted
                            )
                    );
                }

                offset += jobArray.size();

                if (jobArray.size() < limit) {
                    break;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Amazon error: " +
                            e.getMessage()
            );
        }

        System.out.println(
                "TOTAL AMAZON JOBS = " +
                        jobs.size()
        );

        return jobs;
    }

    // =========================================================
    // MICROSOFT - 10 JOBS
    // =========================================================

    private List<CompanyJob> getMicrosoftJobs() {

        List<CompanyJob> jobs =
                new ArrayList<>();

        /*
         * Microsoft India has separate official location
         * pages. We check India + Bengaluru + Hyderabad +
         * Mumbai and collect official career links.
         */

        String[] pages = {

                "https://careers.microsoft.com/v2/global/en/locations/india.html",

                "https://careers.microsoft.com/v2/global/en/locations/bengaluru.html",

                "https://careers.microsoft.com/v2/global/en/locations/hyderabad.html",

                "https://careers.microsoft.com/v2/global/en/locations/mumbai.html",

                "https://careers.microsoft.com/v2/global/en/locations/noida.html"
        };

        Set<String> seen =
                new HashSet<>();

        for (String page : pages) {

            if (jobs.size() >= OTHER_COMPANY_LIMIT) {
                break;
            }

            try {

                Document document =
                        Jsoup.connect(page)
                                .userAgent(
                                        "Mozilla/5.0"
                                )
                                .timeout(30000)
                                .get();

                Elements headings =
                        document.select(
                                "h2, h3, a"
                        );

                for (Element element : headings) {

                    if (jobs.size() >=
                            OTHER_COMPANY_LIMIT) {
                        break;
                    }

                    String title =
                            element.text().trim();

                    String href =
                            element.absUrl("href");

                    if (title.isBlank()) {
                        continue;
                    }

                    /*
                     * Ignore normal navigation text.
                     */
                    if (title.equalsIgnoreCase(
                            "India")) {
                        continue;
                    }

                    if (title.equalsIgnoreCase(
                            "Bengaluru")) {
                        continue;
                    }

                    if (title.equalsIgnoreCase(
                            "Hyderabad")) {
                        continue;
                    }

                    if (title.equalsIgnoreCase(
                            "Mumbai")) {
                        continue;
                    }

                    if (title.equalsIgnoreCase(
                            "Noida")) {
                        continue;
                    }

                    if (title.length() < 8) {
                        continue;
                    }

                    String lower =
                            title.toLowerCase();

                    if (!(lower.contains("engineer")
                            || lower.contains("developer")
                            || lower.contains("manager")
                            || lower.contains("architect")
                            || lower.contains("scientist")
                            || lower.contains("analyst")
                            || lower.contains("consultant")
                            || lower.contains("account")
                            || lower.contains("technology"))) {

                        continue;
                    }

                    String key =
                            title.toLowerCase();

                    if (seen.contains(key)) {
                        continue;
                    }

                    seen.add(key);

                    if (href.isBlank()) {
                        href =
                                "https://careers.microsoft.com/v2/global/en/locations/india.html";
                    }

                    String id =
                            "MS-" +
                                    Math.abs(
                                            title.hashCode()
                                    );

                    jobs.add(
                            new CompanyJob(
                                    "Microsoft",
                                    title,
                                    "India",
                                    "Full Time",
                                    "Official Microsoft Careers opportunity.",
                                    id,
                                    href,
                                    ""
                            )
                    );
                }

            } catch (Exception e) {

                System.out.println(
                        "Microsoft page error: " +
                                e.getMessage()
                );
            }
        }

        System.out.println(
                "TOTAL MICROSOFT JOBS = " +
                        jobs.size()
        );

        return jobs;
    }

    // =========================================================
    // GOOGLE - 10 JOBS
    // =========================================================

    private List<CompanyJob> getGoogleJobs() {

        List<CompanyJob> jobs =
                new ArrayList<>();

        try {

            String url =
                    "https://www.google.com/about/careers/applications/jobs/results"
                            + "?location=India"
                            + "&hl=en";

            Document document =
                    Jsoup.connect(url)
                            .userAgent(
                                    "Mozilla/5.0"
                            )
                            .timeout(30000)
                            .get();

            /*
             * Google currently exposes job result links
             * under /about/careers/applications/jobs/results/
             */

            Elements links =
                    document.select(
                            "a[href*='/about/careers/applications/jobs/results/']"
                    );

            Set<String> seen =
                    new HashSet<>();

            for (Element link : links) {

                if (jobs.size() >=
                        OTHER_COMPANY_LIMIT) {
                    break;
                }

                String title =
                        link.text().trim();

                String href =
                        link.absUrl("href");

                if (title.isBlank() ||
                        href.isBlank()) {
                    continue;
                }

                if (title.length() < 5) {
                    continue;
                }

                if (seen.contains(href)) {
                    continue;
                }

                seen.add(href);

                String id =
                        lastPart(href);

                jobs.add(
                        new CompanyJob(
                                "Google",
                                title,
                                "India",
                                "Full Time",
                                "Official Google Careers opportunity.",
                                id,
                                href,
                                ""
                        )
                );
            }

            /*
             * If normal links are not visible, search
             * the page for job-card elements.
             */

            if (jobs.isEmpty()) {

                Elements cards =
                        document.select(
                                "[data-job-id], " +
                                "[data-jobid], " +
                                "li"
                        );

                for (Element card : cards) {

                    if (jobs.size() >=
                            OTHER_COMPANY_LIMIT) {
                        break;
                    }

                    String title =
                            card.text().trim();

                    if (title.isBlank() ||
                            title.length() < 10) {
                        continue;
                    }

                    if (!title.toLowerCase()
                            .contains("india")) {
                        continue;
                    }

                    String id =
                            "GOOGLE-" +
                                    Math.abs(
                                            title.hashCode()
                                    );

                    String url2 =
                            "https://www.google.com/about/careers/applications/jobs/results?location=India";

                    jobs.add(
                            new CompanyJob(
                                    "Google",
                                    title,
                                    "India",
                                    "Full Time",
                                    "Official Google Careers opportunity.",
                                    id,
                                    url2,
                                    ""
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Google error: " +
                            e.getMessage()
            );
        }

        jobs =
                removeDuplicates(
                        jobs,
                        OTHER_COMPANY_LIMIT
                );

        System.out.println(
                "TOTAL GOOGLE JOBS = " +
                        jobs.size()
        );

        return jobs;
    }

    // =========================================================
    // COGNIZANT - 10 JOBS
    // =========================================================

    private List<CompanyJob> getCognizantJobs() {

        List<CompanyJob> jobs =
                new ArrayList<>();

        try {

            /*
             * The Cognizant India jobs page is an official
             * live career search page.
             */

            String url =
                    "https://careers.cognizant.com/india-en/jobs/";

            Document document =
                    Jsoup.connect(url)
                            .userAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/142 Safari/537.36"
                            )
                            .referrer(
                                    "https://careers.cognizant.com/india-en/"
                            )
                            .timeout(60000)
                            .get();

            Elements links =
                    document.select(
                            "a[href*='/india-en/jobs/']"
                    );

            Set<String> seen =
                    new HashSet<>();

            for (Element link : links) {

                if (jobs.size() >=
                        OTHER_COMPANY_LIMIT) {
                    break;
                }

                String title =
                        link.text().trim();

                String href =
                        link.absUrl("href");

                if (title.isBlank() ||
                        href.isBlank()) {
                    continue;
                }

                if (!href.contains(
                        "/india-en/jobs/")) {
                    continue;
                }

                if (title.length() < 5) {
                    continue;
                }

                if (seen.contains(href)) {
                    continue;
                }

                seen.add(href);

                String id =
                        extractCognizantId(href);

                jobs.add(
                        new CompanyJob(
                                "Cognizant",
                                title,
                                "India",
                                "Full Time",
                                "Official Cognizant Careers opportunity.",
                                id,
                                href,
                                ""
                        )
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Cognizant error: " +
                            e.getMessage()
            );
        }

        System.out.println(
                "TOTAL COGNIZANT JOBS = " +
                        jobs.size()
        );

        return jobs;
    }

    // =========================================================
    // INFOSYS - 10 JOBS
    // =========================================================

    private List<CompanyJob> getInfosysJobs() {

        List<CompanyJob> jobs =
                new ArrayList<>();

        try {

            String url =
                    "https://digitalcareers.infosys.com/infosys/global-careers";

            Document document =
                    Jsoup.connect(url)
                            .userAgent(
                                    "Mozilla/5.0"
                            )
                            .timeout(60000)
                            .get();

            /*
             * Search every link because Infosys changes
             * its job-card HTML frequently.
             */

            Elements links =
                    document.select(
                            "a[href]"
                    );

            Set<String> seen =
                    new HashSet<>();

            for (Element link : links) {

                if (jobs.size() >=
                        OTHER_COMPANY_LIMIT) {
                    break;
                }

                String title =
                        link.text().trim();

                String href =
                        link.absUrl("href");

                if (title.isBlank() ||
                        href.isBlank()) {
                    continue;
                }

                if (!href.contains(
                        "digitalcareers.infosys.com")) {
                    continue;
                }

                String lower =
                        href.toLowerCase();

                if (!(lower.contains("apply")
                        || lower.contains("job")
                        || lower.contains("career"))) {
                    continue;
                }

                if (title.length() < 5) {
                    continue;
                }

                if (seen.contains(href)) {
                    continue;
                }

                seen.add(href);

                String id =
                        lastPart(href);

                jobs.add(
                        new CompanyJob(
                                "Infosys",
                                title,
                                "India",
                                "Full Time",
                                "Official Infosys Careers opportunity.",
                                id,
                                href,
                                ""
                        )
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Infosys error: " +
                            e.getMessage()
            );
        }

        System.out.println(
                "TOTAL INFOSYS JOBS = " +
                        jobs.size()
        );

        return jobs;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String text(
            JsonNode node,
            String field) {

        if (node == null ||
                !node.has(field) ||
                node.get(field).isNull()) {
            return "";
        }

        return node.get(field)
                .asText("")
                .trim();
    }

    private String clean(
            String value) {

        if (value == null ||
                value.isBlank()) {
            return "Official company career opportunity.";
        }

        String result =
                Jsoup.parse(value)
                        .text()
                        .trim();

        if (result.length() > 1000) {
            result =
                    result.substring(0, 1000)
                            + "...";
        }

        return result;
    }

    private String lastPart(
            String url) {

        try {

            String value =
                    url.split("\\?")[0];

            while (value.endsWith("/")) {
                value =
                        value.substring(
                                0,
                                value.length() - 1
                        );
            }

            int index =
                    value.lastIndexOf("/");

            if (index >= 0) {
                return value.substring(
                        index + 1
                );
            }

        } catch (Exception ignored) {
        }

        return "JOB-" +
                Math.abs(url.hashCode());
    }

    private String extractCognizantId(
            String url) {

        String value =
                lastPart(url);

        if (value.matches("\\d+")) {
            return value;
        }

        return "COG-" +
                Math.abs(url.hashCode());
    }

    private List<CompanyJob> removeDuplicates(
            List<CompanyJob> jobs,
            int limit) {

        List<CompanyJob> result =
                new ArrayList<>();

        Set<String> seen =
                new HashSet<>();

        for (CompanyJob job : jobs) {

            if (result.size() >= limit) {
                break;
            }

            if (job == null) {
                continue;
            }

            String key =
                    (
                            job.getCompany()
                                    + "|"
                                    + job.getTitle()
                    ).toLowerCase();

            if (seen.contains(key)) {
                continue;
            }

            seen.add(key);
            result.add(job);
        }

        return result;
    }
}
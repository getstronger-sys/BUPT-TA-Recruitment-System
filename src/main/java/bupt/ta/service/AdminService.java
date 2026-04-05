package bupt.ta.service;

import bupt.ta.model.AdminSettings;
import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared admin analytics and workload-enforcement logic.
 */
public class AdminService {

    public static final String STATUS_AUTO_CLOSED = "AUTO_CLOSED";
    private static final String AUTO_CLOSE_NOTE_PREFIX = "[System] Closed automatically because the applicant reached the workload limit of ";

    public static class DashboardSummary {
        private final int totalJobs;
        private final int openJobs;
        private final int inactiveJobs;
        private final int totalApplications;
        private final int totalTas;
        private final int totalMos;
        private final int totalAdmins;
        private final int tasAtOrOverLimit;
        private final int jobsAtCapacity;
        private final Map<String, Integer> applicationCounts;

        public DashboardSummary(int totalJobs, int openJobs, int inactiveJobs, int totalApplications,
                                int totalTas, int totalMos, int totalAdmins, int tasAtOrOverLimit,
                                int jobsAtCapacity, Map<String, Integer> applicationCounts) {
            this.totalJobs = totalJobs;
            this.openJobs = openJobs;
            this.inactiveJobs = inactiveJobs;
            this.totalApplications = totalApplications;
            this.totalTas = totalTas;
            this.totalMos = totalMos;
            this.totalAdmins = totalAdmins;
            this.tasAtOrOverLimit = tasAtOrOverLimit;
            this.jobsAtCapacity = jobsAtCapacity;
            this.applicationCounts = applicationCounts != null ? applicationCounts : Collections.emptyMap();
        }

        public int getTotalJobs() { return totalJobs; }
        public int getOpenJobs() { return openJobs; }
        public int getInactiveJobs() { return inactiveJobs; }
        public int getTotalApplications() { return totalApplications; }
        public int getTotalTas() { return totalTas; }
        public int getTotalMos() { return totalMos; }
        public int getTotalAdmins() { return totalAdmins; }
        public int getTasAtOrOverLimit() { return tasAtOrOverLimit; }
        public int getJobsAtCapacity() { return jobsAtCapacity; }
        public Map<String, Integer> getApplicationCounts() { return applicationCounts; }
        public int getCount(String status) { return applicationCounts.getOrDefault(status, 0); }
    }

    public static class WorkloadRow {
        private final String applicantId;
        private final String applicantName;
        private final int selectedCount;
        private final int pendingCount;
        private final boolean aboveAverage;
        private final boolean atOrOverLimit;
        private final boolean aboveLimit;
        private final List<String> selectedJobTitles;

        public WorkloadRow(String applicantId, String applicantName, int selectedCount, int pendingCount,
                           boolean aboveAverage, boolean atOrOverLimit, boolean aboveLimit,
                           List<String> selectedJobTitles) {
            this.applicantId = applicantId;
            this.applicantName = applicantName;
            this.selectedCount = selectedCount;
            this.pendingCount = pendingCount;
            this.aboveAverage = aboveAverage;
            this.atOrOverLimit = atOrOverLimit;
            this.aboveLimit = aboveLimit;
            this.selectedJobTitles = selectedJobTitles != null ? selectedJobTitles : Collections.emptyList();
        }

        public String getApplicantId() { return applicantId; }
        public String getApplicantName() { return applicantName; }
        public int getSelectedCount() { return selectedCount; }
        public int getPendingCount() { return pendingCount; }
        public boolean isAboveAverage() { return aboveAverage; }
        public boolean isAtOrOverLimit() { return atOrOverLimit; }
        public boolean isAboveLimit() { return aboveLimit; }
        public List<String> getSelectedJobTitles() { return selectedJobTitles; }
    }

    public static class LimitAlert {
        private final String applicantId;
        private final String applicantName;
        private final int selectedCount;
        private final int pendingCount;

        public LimitAlert(String applicantId, String applicantName, int selectedCount, int pendingCount) {
            this.applicantId = applicantId;
            this.applicantName = applicantName;
            this.selectedCount = selectedCount;
            this.pendingCount = pendingCount;
        }

        public String getApplicantId() { return applicantId; }
        public String getApplicantName() { return applicantName; }
        public int getSelectedCount() { return selectedCount; }
        public int getPendingCount() { return pendingCount; }
    }

    public static class InterviewNoticeAlert {
        private final String applicationId;
        private final String applicantName;
        private final String jobTitle;
        private final String moduleCode;
        private final boolean missingTime;
        private final boolean missingLocation;

        public InterviewNoticeAlert(String applicationId, String applicantName, String jobTitle, String moduleCode,
                                    boolean missingTime, boolean missingLocation) {
            this.applicationId = applicationId;
            this.applicantName = applicantName;
            this.jobTitle = jobTitle;
            this.moduleCode = moduleCode;
            this.missingTime = missingTime;
            this.missingLocation = missingLocation;
        }

        public String getApplicationId() { return applicationId; }
        public String getApplicantName() { return applicantName; }
        public String getJobTitle() { return jobTitle; }
        public String getModuleCode() { return moduleCode; }
        public boolean isMissingTime() { return missingTime; }
        public boolean isMissingLocation() { return missingLocation; }
    }

    public static class ApplicationAlert {
        private final String applicationId;
        private final String applicantName;
        private final String jobTitle;
        private final String moduleCode;
        private final String status;
        private final String issue;

        public ApplicationAlert(String applicationId, String applicantName, String jobTitle,
                                String moduleCode, String status, String issue) {
            this.applicationId = applicationId;
            this.applicantName = applicantName;
            this.jobTitle = jobTitle;
            this.moduleCode = moduleCode;
            this.status = status;
            this.issue = issue;
        }

        public String getApplicationId() { return applicationId; }
        public String getApplicantName() { return applicantName; }
        public String getJobTitle() { return jobTitle; }
        public String getModuleCode() { return moduleCode; }
        public String getStatus() { return status; }
        public String getIssue() { return issue; }
    }

    public static class CapacityAlert {
        private final String jobId;
        private final String jobTitle;
        private final String moduleCode;
        private final int selectedCount;
        private final int maxApplicants;

        public CapacityAlert(String jobId, String jobTitle, String moduleCode, int selectedCount, int maxApplicants) {
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.moduleCode = moduleCode;
            this.selectedCount = selectedCount;
            this.maxApplicants = maxApplicants;
        }

        public String getJobId() { return jobId; }
        public String getJobTitle() { return jobTitle; }
        public String getModuleCode() { return moduleCode; }
        public int getSelectedCount() { return selectedCount; }
        public int getMaxApplicants() { return maxApplicants; }
    }

    public static class MonitoringReport {
        private final List<LimitAlert> limitAlerts;
        private final List<InterviewNoticeAlert> interviewNoticeAlerts;
        private final List<ApplicationAlert> inactiveJobAlerts;
        private final List<ApplicationAlert> missingJobAlerts;
        private final List<CapacityAlert> capacityAlerts;

        public MonitoringReport(List<LimitAlert> limitAlerts,
                                List<InterviewNoticeAlert> interviewNoticeAlerts,
                                List<ApplicationAlert> inactiveJobAlerts,
                                List<ApplicationAlert> missingJobAlerts,
                                List<CapacityAlert> capacityAlerts) {
            this.limitAlerts = limitAlerts != null ? limitAlerts : Collections.emptyList();
            this.interviewNoticeAlerts = interviewNoticeAlerts != null ? interviewNoticeAlerts : Collections.emptyList();
            this.inactiveJobAlerts = inactiveJobAlerts != null ? inactiveJobAlerts : Collections.emptyList();
            this.missingJobAlerts = missingJobAlerts != null ? missingJobAlerts : Collections.emptyList();
            this.capacityAlerts = capacityAlerts != null ? capacityAlerts : Collections.emptyList();
        }

        public List<LimitAlert> getLimitAlerts() { return limitAlerts; }
        public List<InterviewNoticeAlert> getInterviewNoticeAlerts() { return interviewNoticeAlerts; }
        public List<ApplicationAlert> getInactiveJobAlerts() { return inactiveJobAlerts; }
        public List<ApplicationAlert> getMissingJobAlerts() { return missingJobAlerts; }
        public List<CapacityAlert> getCapacityAlerts() { return capacityAlerts; }
        public int getTotalIssues() {
            return limitAlerts.size() + interviewNoticeAlerts.size() + inactiveJobAlerts.size()
                    + missingJobAlerts.size() + capacityAlerts.size();
        }
    }

    public DashboardSummary buildDashboardSummary(DataStorage storage, AdminSettings settings) throws IOException {
        List<User> users = storage.loadUsers();
        List<Job> jobs = storage.loadJobs();
        List<Application> apps = storage.loadApplications();

        int totalTas = (int) users.stream().filter(u -> "TA".equals(u.getRole())).count();
        int totalMos = (int) users.stream().filter(u -> "MO".equals(u.getRole())).count();
        int totalAdmins = (int) users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        int openJobs = (int) jobs.stream().filter(JobActivity::isActive).count();
        int inactiveJobs = jobs.size() - openJobs;

        Map<String, Integer> applicationCounts = new LinkedHashMap<>();
        applicationCounts.put("PENDING", 0);
        applicationCounts.put("INTERVIEW", 0);
        applicationCounts.put("SELECTED", 0);
        applicationCounts.put("REJECTED", 0);
        applicationCounts.put(STATUS_AUTO_CLOSED, 0);
        applicationCounts.put("WITHDRAWN", 0);
        for (Application app : apps) {
            String status = normalizeStatus(app.getStatus());
            applicationCounts.put(status, applicationCounts.getOrDefault(status, 0) + 1);
        }

        Map<String, Long> selectedByApplicant = apps.stream()
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .collect(Collectors.groupingBy(Application::getApplicantId, Collectors.counting()));
        int tasAtOrOverLimit = 0;
        if (settings != null && settings.hasWorkloadLimit()) {
            int limit = settings.getMaxSelectedJobsPerTa();
            tasAtOrOverLimit = (int) selectedByApplicant.values().stream().filter(v -> v >= limit).count();
        }

        Map<String, Long> selectedByJob = apps.stream()
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .collect(Collectors.groupingBy(Application::getJobId, Collectors.counting()));
        int jobsAtCapacity = 0;
        for (Job job : jobs) {
            if (job.getMaxApplicants() > 0 && selectedByJob.getOrDefault(job.getId(), 0L) >= job.getMaxApplicants()) {
                jobsAtCapacity++;
            }
        }

        return new DashboardSummary(
                jobs.size(),
                openJobs,
                inactiveJobs,
                apps.size(),
                totalTas,
                totalMos,
                totalAdmins,
                tasAtOrOverLimit,
                jobsAtCapacity,
                applicationCounts
        );
    }

    public List<WorkloadRow> buildWorkloadRows(DataStorage storage, AdminSettings settings) throws IOException {
        List<Application> apps = storage.loadApplications();
        List<Job> jobs = storage.loadJobs();
        List<User> users = storage.loadUsers();

        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<String, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a));

        Map<String, Integer> selectedByApplicant = new HashMap<>();
        Map<String, Integer> pendingByApplicant = new HashMap<>();
        Map<String, List<String>> selectedTitles = new HashMap<>();
        for (Application app : apps) {
            String applicantId = app.getApplicantId();
            if ("SELECTED".equals(app.getStatus())) {
                selectedByApplicant.merge(applicantId, 1, Integer::sum);
                Job job = jobMap.get(app.getJobId());
                selectedTitles.computeIfAbsent(applicantId, key -> new ArrayList<>())
                        .add(job != null ? job.getTitle() : app.getJobId());
            } else if ("PENDING".equals(app.getStatus())) {
                pendingByApplicant.merge(applicantId, 1, Integer::sum);
            }
        }

        double avgSelected = selectedByApplicant.isEmpty()
                ? 0
                : selectedByApplicant.values().stream().mapToInt(Integer::intValue).average().orElse(0);

        List<WorkloadRow> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedByApplicant.entrySet()) {
            String applicantId = entry.getKey();
            int selectedCount = entry.getValue();
            int pendingCount = pendingByApplicant.getOrDefault(applicantId, 0);
            User user = userMap.get(applicantId);
            boolean atOrOverLimit = settings != null && settings.hasWorkloadLimit()
                    && selectedCount >= settings.getMaxSelectedJobsPerTa();
            boolean aboveLimit = settings != null && settings.hasWorkloadLimit()
                    && selectedCount > settings.getMaxSelectedJobsPerTa();
            rows.add(new WorkloadRow(
                    applicantId,
                    resolveUserName(user, applicantId),
                    selectedCount,
                    pendingCount,
                    selectedCount > avgSelected,
                    atOrOverLimit,
                    aboveLimit,
                    selectedTitles.getOrDefault(applicantId, Collections.emptyList())
            ));
        }

        rows.sort(Comparator
                .comparing(WorkloadRow::isAboveLimit).reversed()
                .thenComparing(WorkloadRow::isAtOrOverLimit).reversed()
                .thenComparing(WorkloadRow::getSelectedCount, Comparator.reverseOrder())
                .thenComparing(WorkloadRow::getApplicantName, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    public int enforceWorkloadLimitForApplicant(DataStorage storage, String applicantId, String keepApplicationId,
                                                AdminSettings settings) throws IOException {
        if (applicantId == null || applicantId.trim().isEmpty()) {
            return 0;
        }
        if (settings == null || !settings.hasWorkloadLimit() || !settings.isAutoClosePendingWhenLimitReached()) {
            return 0;
        }

        List<Application> apps = storage.loadApplications();
        long selectedCount = apps.stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .count();
        if (selectedCount < settings.getMaxSelectedJobsPerTa()) {
            return 0;
        }

        int closed = 0;
        for (Application app : apps) {
            if (!applicantId.equals(app.getApplicantId())) {
                continue;
            }
            if (keepApplicationId != null && keepApplicationId.equals(app.getId())) {
                continue;
            }
            if (!"PENDING".equals(app.getStatus())) {
                continue;
            }
            app.setStatus(STATUS_AUTO_CLOSED);
            app.setNotes(appendAutoCloseNote(app.getNotes(), settings.getMaxSelectedJobsPerTa()));
            storage.saveApplication(app);
            Job closedJob = storage.getJobById(app.getJobId());
            StudentNotificationService.notifyAutoClosed(storage, app, closedJob, app.getNotes());
            closed++;
        }
        return closed;
    }

    public int enforceWorkloadLimitGlobally(DataStorage storage, AdminSettings settings) throws IOException {
        if (settings == null || !settings.hasWorkloadLimit() || !settings.isAutoClosePendingWhenLimitReached()) {
            return 0;
        }
        List<Application> apps = storage.loadApplications();
        int totalClosed = 0;
        List<String> applicantIds = apps.stream()
                .map(Application::getApplicantId)
                .distinct()
                .collect(Collectors.toList());
        for (String applicantId : applicantIds) {
            totalClosed += enforceWorkloadLimitForApplicant(storage, applicantId, null, settings);
        }
        return totalClosed;
    }

    public MonitoringReport buildMonitoringReport(DataStorage storage, AdminSettings settings) throws IOException {
        List<User> users = storage.loadUsers();
        List<Job> jobs = storage.loadJobs();
        List<Application> apps = storage.loadApplications();

        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<String, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a));

        List<LimitAlert> limitAlerts = new ArrayList<>();
        if (settings != null && settings.hasWorkloadLimit()) {
            Map<String, Long> selectedByApplicant = apps.stream()
                    .filter(a -> "SELECTED".equals(a.getStatus()))
                    .collect(Collectors.groupingBy(Application::getApplicantId, Collectors.counting()));
            Map<String, Long> pendingByApplicant = apps.stream()
                    .filter(a -> "PENDING".equals(a.getStatus()))
                    .collect(Collectors.groupingBy(Application::getApplicantId, Collectors.counting()));
            for (Map.Entry<String, Long> entry : selectedByApplicant.entrySet()) {
                long pendingCount = pendingByApplicant.getOrDefault(entry.getKey(), 0L);
                if (entry.getValue() >= settings.getMaxSelectedJobsPerTa() && pendingCount > 0) {
                    User user = userMap.get(entry.getKey());
                    limitAlerts.add(new LimitAlert(
                            entry.getKey(),
                            resolveUserName(user, entry.getKey()),
                            entry.getValue().intValue(),
                            (int) pendingCount
                    ));
                }
            }
            limitAlerts.sort(Comparator.comparing(LimitAlert::getSelectedCount, Comparator.reverseOrder()));
        }

        List<InterviewNoticeAlert> interviewNoticeAlerts = new ArrayList<>();
        List<ApplicationAlert> inactiveJobAlerts = new ArrayList<>();
        List<ApplicationAlert> missingJobAlerts = new ArrayList<>();
        for (Application app : apps) {
            Job job = jobMap.get(app.getJobId());
            User user = userMap.get(app.getApplicantId());
            String applicantName = resolveUserName(user, app.getApplicantId());

            if ("INTERVIEW".equals(app.getStatus())) {
                boolean missingTime = isBlank(app.getInterviewTime());
                boolean missingLocation = isBlank(app.getInterviewLocation());
                if (missingTime || missingLocation) {
                    interviewNoticeAlerts.add(new InterviewNoticeAlert(
                            app.getId(),
                            applicantName,
                            job != null ? job.getTitle() : app.getJobId(),
                            job != null ? job.getModuleCode() : "-",
                            missingTime,
                            missingLocation
                    ));
                }
            }

            if (job == null) {
                missingJobAlerts.add(new ApplicationAlert(
                        app.getId(),
                        applicantName,
                        app.getJobId(),
                        "-",
                        normalizeStatus(app.getStatus()),
                        "Application references a job that does not exist."
                ));
                continue;
            }

            if (("PENDING".equals(app.getStatus()) || "INTERVIEW".equals(app.getStatus())) && JobActivity.isInactive(job)) {
                inactiveJobAlerts.add(new ApplicationAlert(
                        app.getId(),
                        applicantName,
                        job.getTitle(),
                        job.getModuleCode(),
                        normalizeStatus(app.getStatus()),
                        "Application is still active although the job is closed or past deadline."
                ));
            }
        }

        Map<String, Long> selectedByJob = apps.stream()
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .collect(Collectors.groupingBy(Application::getJobId, Collectors.counting()));
        List<CapacityAlert> capacityAlerts = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getMaxApplicants() <= 0) {
                continue;
            }
            long selectedCount = selectedByJob.getOrDefault(job.getId(), 0L);
            if (selectedCount > job.getMaxApplicants()) {
                capacityAlerts.add(new CapacityAlert(
                        job.getId(),
                        job.getTitle(),
                        job.getModuleCode(),
                        (int) selectedCount,
                        job.getMaxApplicants()
                ));
            }
        }
        capacityAlerts.sort(Comparator.comparing(CapacityAlert::getSelectedCount, Comparator.reverseOrder()));

        return new MonitoringReport(limitAlerts, interviewNoticeAlerts, inactiveJobAlerts, missingJobAlerts, capacityAlerts);
    }

    public static String normalizeStatus(String status) {
        return status == null || status.trim().isEmpty() ? "UNKNOWN" : status.trim().toUpperCase();
    }

    private static String resolveUserName(User user, String fallback) {
        if (user == null) {
            return fallback != null ? fallback : "-";
        }
        if (!isBlank(user.getRealName())) {
            return user.getRealName().trim();
        }
        if (!isBlank(user.getUsername())) {
            return user.getUsername().trim();
        }
        return fallback != null ? fallback : "-";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String appendAutoCloseNote(String existing, int limit) {
        String autoNote = AUTO_CLOSE_NOTE_PREFIX + limit + ".";
        if (isBlank(existing)) {
            return autoNote;
        }
        String trimmed = existing.trim();
        if (trimmed.contains(autoNote)) {
            return trimmed;
        }
        return trimmed + " | " + autoNote;
    }
}

package bupt.ta.service;

import bupt.ta.model.AdminSettings;
import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdminServiceTest {

    private final AdminService adminService = new AdminService();

    @Test
    public void testAdminSettingsPersistence() throws Exception {
        Path tmp = Files.createTempDirectory("ta-admin-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            AdminSettings settings = storage.loadAdminSettings();
            assertNotNull(settings);
            assertEquals(2, settings.getMaxSelectedJobsPerTa());
            assertTrue(settings.isAutoClosePendingWhenLimitReached());

            settings.setMaxSelectedJobsPerTa(3);
            settings.setAutoClosePendingWhenLimitReached(false);
            storage.saveAdminSettings(settings);

            AdminSettings reloaded = storage.loadAdminSettings();
            assertEquals(3, reloaded.getMaxSelectedJobsPerTa());
            assertFalse(reloaded.isAutoClosePendingWhenLimitReached());
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testAutoClosePendingApplicationsWhenLimitReached() throws Exception {
        Path tmp = Files.createTempDirectory("ta-admin-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            User applicant = createUser(storage, "ta-extra", "Applicant One");

            Job selectedJob = createJob(storage, "Selected Job", "EBU7001", 3);
            Job pendingJob = createJob(storage, "Pending Job", "EBU7002", 3);
            Job interviewJob = createJob(storage, "Interview Job", "EBU7003", 3);

            Application selected = createApplication(storage, selectedJob.getId(), applicant.getId(), applicant.getRealName(), "SELECTED");
            Application pending = createApplication(storage, pendingJob.getId(), applicant.getId(), applicant.getRealName(), "PENDING");
            Application interview = createApplication(storage, interviewJob.getId(), applicant.getId(), applicant.getRealName(), "INTERVIEW");

            AdminSettings settings = new AdminSettings();
            settings.setMaxSelectedJobsPerTa(1);
            settings.setAutoClosePendingWhenLimitReached(true);

            int closed = adminService.enforceWorkloadLimitForApplicant(storage, applicant.getId(), selected.getId(), settings);
            assertEquals(1, closed);

            Application pendingReloaded = storage.loadApplications().stream()
                    .filter(a -> a.getId().equals(pending.getId()))
                    .findFirst().orElse(null);
            Application interviewReloaded = storage.loadApplications().stream()
                    .filter(a -> a.getId().equals(interview.getId()))
                    .findFirst().orElse(null);

            assertNotNull(pendingReloaded);
            assertEquals(AdminService.STATUS_AUTO_CLOSED, pendingReloaded.getStatus());
            assertTrue(pendingReloaded.getNotes().contains("workload limit"));
            assertNotNull(interviewReloaded);
            assertEquals("INTERVIEW", interviewReloaded.getStatus());
        } finally {
            deleteRecursive(tmp);
        }
    }

    @Test
    public void testMonitoringReportFindsExpectedIssues() throws Exception {
        Path tmp = Files.createTempDirectory("ta-admin-test");
        try {
            DataStorage storage = new DataStorage(tmp.toString());
            User applicant = createUser(storage, "ta-monitor", "Applicant Two");
            User applicant2 = createUser(storage, "ta-monitor-2", "Applicant Three");

            Job openJob = createJob(storage, "Open Job", "EBU7100", 3);
            Job closedJob = createJob(storage, "Closed Job", "EBU7101", 3);
            closedJob.setStatus("CLOSED");
            storage.saveJob(closedJob);

            Job cappedJob = createJob(storage, "Capped Job", "EBU7102", 1);

            createApplication(storage, openJob.getId(), applicant.getId(), applicant.getRealName(), "SELECTED");
            createApplication(storage, openJob.getId(), applicant.getId(), applicant.getRealName(), "PENDING");
            createApplication(storage, openJob.getId(), applicant.getId(), applicant.getRealName(), "INTERVIEW");
            createApplication(storage, closedJob.getId(), applicant.getId(), applicant.getRealName(), "PENDING");
            createApplication(storage, "J9999", applicant.getId(), applicant.getRealName(), "PENDING");

            createApplication(storage, cappedJob.getId(), applicant.getId(), applicant.getRealName(), "SELECTED");
            createApplication(storage, cappedJob.getId(), applicant2.getId(), applicant2.getRealName(), "SELECTED");

            AdminSettings settings = new AdminSettings();
            settings.setMaxSelectedJobsPerTa(1);
            settings.setAutoClosePendingWhenLimitReached(true);

            AdminService.MonitoringReport report = adminService.buildMonitoringReport(storage, settings);
            assertEquals(1, report.getLimitAlerts().size());
            assertEquals(1, report.getInterviewNoticeAlerts().size());
            assertEquals(1, report.getInactiveJobAlerts().size());
            assertEquals(1, report.getMissingJobAlerts().size());
            assertEquals(1, report.getCapacityAlerts().size());
        } finally {
            deleteRecursive(tmp);
        }
    }

    private static User createUser(DataStorage storage, String username, String realName) throws IOException {
        User user = new User();
        user.setUsername(username);
        user.setPassword("test123");
        user.setRole("TA");
        user.setEmail(username + "@example.com");
        user.setRealName(realName);
        return storage.addUser(user);
    }

    private static Job createJob(DataStorage storage, String title, String moduleCode, int maxApplicants) throws IOException {
        Job job = new Job();
        job.setTitle(title);
        job.setModuleCode(moduleCode);
        job.setModuleName(title);
        job.setPostedBy("U003");
        job.setPostedByName("Wang MO");
        job.setMaxApplicants(maxApplicants);
        return storage.addJob(job);
    }

    private static Application createApplication(DataStorage storage, String jobId, String applicantId,
                                                 String applicantName, String status) throws IOException {
        Application app = new Application();
        app.setJobId(jobId);
        app.setApplicantId(applicantId);
        app.setApplicantName(applicantName);
        storage.addApplication(app);
        app.setStatus(status);
        storage.saveApplication(app);
        return app;
    }

    private static void deleteRecursive(Path p) throws IOException {
        if (Files.exists(p)) {
            Files.walk(p).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
                try { Files.delete(path); } catch (IOException ignored) {}
            });
        }
    }
}

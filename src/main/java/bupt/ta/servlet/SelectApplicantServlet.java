package bupt.ta.servlet;

import bupt.ta.model.AdminSettings;
import bupt.ta.model.Application;
import bupt.ta.model.InterviewEvaluation;
import bupt.ta.model.Job;
import bupt.ta.service.AdminService;
import bupt.ta.service.ApplicationTimelineService;
import bupt.ta.service.StudentNotificationService;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;
import bupt.ta.util.JobSelectionCapacity;
import bupt.ta.util.MoJobsRedirectParams;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MO pipeline actions: move to interview/waitlist, or final select/reject (waitlist only).
 */
public class SelectApplicantServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final ApplicationTimelineService timelineService = new ApplicationTimelineService();

    private static void redirectJobs(HttpServletResponse resp, HttpServletRequest req, String listPath, String view,
                                     String jobId, String extraQuery, String expandAppId) throws IOException {
        String ctx = req.getContextPath();
        StringBuilder b = new StringBuilder(ctx).append(listPath).append("?");
        if (jobId != null && !jobId.trim().isEmpty()) {
            b.append("jobId=").append(URLEncoder.encode(jobId.trim(), StandardCharsets.UTF_8)).append("&");
        }
        b.append("view=").append(view);
        if (extraQuery != null && !extraQuery.isEmpty()) {
            b.append("&").append(extraQuery);
        }
        MoJobsRedirectParams.appendExpandApp(b, view, expandAppId);
        resp.sendRedirect(b.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String appId = req.getParameter("applicationId");
        String action = req.getParameter("action");  // interview, waitlist, select or reject
        String notes = req.getParameter("notes");
        String decisionReason = trim(req.getParameter("decisionReason"));
        String applicantFeedback = trim(req.getParameter("applicantFeedback"));
        String moId = (String) req.getSession().getAttribute("userId");
        String moName = (String) req.getSession().getAttribute("realName");

        if (appId == null || appId.trim().isEmpty()) {
            redirectJobs(resp, req, JobActivity.PATH_ACTIVE, "pending", null, "error=invalid", null);
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        List<Application> apps = storage.loadApplications();
        Application target = apps.stream().filter(a -> a.getId().equals(appId)).findFirst().orElse(null);

        if (target == null) {
            redirectJobs(resp, req, JobActivity.PATH_ACTIVE, "pending", null, "error=not_found", null);
            return;
        }

        String jobId = target.getJobId();
        Job job = storage.getJobById(jobId);
        String listPath = job != null ? JobActivity.listPathFor(job) : JobActivity.PATH_ACTIVE;
        if (job == null || !moId.equals(job.getPostedBy())) {
            redirectJobs(resp, req, listPath, "pending", jobId, "error=forbidden", null);
            return;
        }
        if (JobActivity.isInactive(job)) {
            redirectJobs(resp, req, JobActivity.PATH_INACTIVE, "pending", jobId, "error=job_inactive", null);
            return;
        }

        String fromStatus = target.getStatus();
        if ("interview".equalsIgnoreCase(action)) {
            if (!"PENDING".equals(target.getStatus())) {
                redirectJobs(resp, req, listPath, "pending", jobId, "error=not_pending", null);
                return;
            }
            target.setStatus("INTERVIEW");
        } else if ("waitlist".equalsIgnoreCase(action)) {
            if (!"INTERVIEW".equals(target.getStatus())) {
                redirectJobs(resp, req, listPath, "interview", jobId, "error=not_interview", appId);
                return;
            }
            if (storage.getInterviewEvaluationByApplicationId(target.getId()) == null) {
                redirectJobs(resp, req, listPath, "interview", jobId, "error=evaluation_required", appId);
                return;
            }
            target.setStatus("WAITLIST");
        } else if ("select".equalsIgnoreCase(action)) {
            if (!"WAITLIST".equals(target.getStatus())) {
                redirectJobs(resp, req, listPath, viewForStatus(target.getStatus()), jobId, "error=not_waitlist", null);
                return;
            }
            InterviewEvaluation evaluation = storage.getInterviewEvaluationByApplicationId(target.getId());
            if (evaluation == null) {
                redirectJobs(resp, req, listPath, "waitlist", jobId, "error=evaluation_required", null);
                return;
            }
            if (decisionReason.isEmpty()) {
                decisionReason = trim(notes);
            }
            if (decisionReason.isEmpty()) {
                redirectJobs(resp, req, listPath, "waitlist", jobId, "error=decision_reason_required", null);
                return;
            }
            if (!JobSelectionCapacity.hasVacancy(job, storage.getApplicationsByJobId(jobId), target.getId())) {
                redirectJobs(resp, req, listPath, "waitlist", jobId, "error=capacity_reached", null);
                return;
            }
            AdminSettings settings = storage.loadAdminSettings();
            if (adminService.wouldExceedWorkloadLimitOnSelect(storage, target.getApplicantId(), job, settings)) {
                redirectJobs(resp, req, listPath, "waitlist", jobId, "error=ta_workload_cap", null);
                return;
            }
            target.setStatus("SELECTED");
        } else if ("reject".equalsIgnoreCase(action)) {
            if (!"WAITLIST".equals(target.getStatus())) {
                redirectJobs(resp, req, listPath, viewForStatus(target.getStatus()), jobId, "error=not_waitlist", null);
                return;
            }
            target.setStatus("REJECTED");
        } else {
            redirectJobs(resp, req, listPath, "pending", jobId, "error=invalid_action", null);
            return;
        }
        target.setNotes(notes != null ? notes.trim() : "");
        if ("select".equalsIgnoreCase(action) || "reject".equalsIgnoreCase(action)) {
            target.setDecisionReason(decisionReason);
            target.setApplicantFeedback(applicantFeedback);
        }
        storage.saveApplication(target);
        if ("select".equalsIgnoreCase(action) || "reject".equalsIgnoreCase(action)) {
            timelineService.record(storage, target, job, moId, moName, "MO",
                    ApplicationTimelineService.TYPE_DECISION_RECORDED,
                    "Decision recorded",
                    decisionReason.isEmpty() ? target.getStatus() : decisionReason,
                    fromStatus, target.getStatus());
        } else {
            timelineService.recordStatusChange(storage, target, job, moId, moName, "MO",
                    fromStatus, target.getStatus(),
                    notes != null && !notes.trim().isEmpty() ? notes.trim() : "Moved by module organiser.");
        }

        if ("interview".equalsIgnoreCase(action)) {
            StudentNotificationService.notifyInterviewInvite(storage, target, job);
        } else if ("waitlist".equalsIgnoreCase(action)) {
            StudentNotificationService.notifyWaitlist(storage, target, job);
        } else if ("select".equalsIgnoreCase(action)) {
            StudentNotificationService.notifySelected(storage, target, job);
        } else if ("reject".equalsIgnoreCase(action)) {
            StudentNotificationService.notifyRejected(storage, target, job, target.getNotes());
        }

        String extraQuery = "updated=1";
        if ("select".equalsIgnoreCase(action)) {
            AdminSettings settings = storage.loadAdminSettings();
            int autoClosed = adminService.enforceWorkloadLimitForApplicant(storage, target.getApplicantId(), target.getId(), settings);
            if (autoClosed > 0) {
                extraQuery += "&autoClosed=" + autoClosed;
            }
        }

        String view = "pending";
        if ("interview".equalsIgnoreCase(action)) {
            view = "interview";
        } else if ("waitlist".equalsIgnoreCase(action)) {
            view = "waitlist";
        } else if ("select".equalsIgnoreCase(action) || "reject".equalsIgnoreCase(action)) {
            view = "outcome";
        }
        Job jobAfter = storage.getJobById(jobId);
        String pathAfter = jobAfter != null ? JobActivity.listPathFor(jobAfter) : listPath;
        String expandAppId = "interview".equals(view) ? appId : null;
        redirectJobs(resp, req, pathAfter, view, jobId, extraQuery, expandAppId);
    }

    private static String viewForStatus(String status) {
        if ("INTERVIEW".equals(status)) {
            return "interview";
        }
        if ("WAITLIST".equals(status)) {
            return "waitlist";
        }
        if ("SELECTED".equals(status) || "REJECTED".equals(status) || "AUTO_CLOSED".equals(status)) {
            return "outcome";
        }
        if ("WITHDRAWN".equals(status)) {
            return "withdrawn";
        }
        return "pending";
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

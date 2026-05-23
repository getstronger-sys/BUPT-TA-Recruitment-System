package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.InterviewEvaluation;
import bupt.ta.model.Job;
import bupt.ta.service.ApplicationTimelineService;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;
import bupt.ta.util.MoJobsRedirectParams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saves or updates an MO interview evaluation for an applicant in INTERVIEW stage.
 */
public class MOInterviewEvaluationServlet extends HttpServlet {
    private static final Set<String> RECOMMENDATIONS = new HashSet<>(
            Arrays.asList("STRONG_HIRE", "HIRE", "WAITLIST", "REJECT"));
    private final ApplicationTimelineService timelineService = new ApplicationTimelineService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String applicationId = trim(req.getParameter("applicationId"));
        String moId = (String) req.getSession().getAttribute("userId");
        String evaluatorName = (String) req.getSession().getAttribute("realName");

        if (applicationId.isEmpty()) {
            redirectToJobs(req, resp, null, "interview", "invalid");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Application app = findApplication(storage.loadApplications(), applicationId);
        if (app == null) {
            redirectToJobs(req, resp, null, "interview", "not_found");
            return;
        }

        Job job = storage.getJobById(app.getJobId());
        if (job == null || !moId.equals(job.getPostedBy())) {
            redirectToJobs(req, resp, app.getJobId(), "interview", "forbidden");
            return;
        }
        if (JobActivity.isInactive(job)) {
            redirectToJobs(req, resp, app.getJobId(), "interview", "job_inactive");
            return;
        }

        if (!"INTERVIEW".equals(app.getStatus())) {
            redirectToJobs(req, resp, app.getJobId(), "interview", "evaluation_status");
            return;
        }

        String recommendation = trim(req.getParameter("recommendation")).toUpperCase();
        if (!RECOMMENDATIONS.contains(recommendation)) {
            redirectToJobs(req, resp, app.getJobId(), "interview", "evaluation_invalid");
            return;
        }

        InterviewEvaluation evaluation = storage.getInterviewEvaluationByApplicationId(applicationId);
        if (evaluation == null) {
            evaluation = new InterviewEvaluation();
        }
        evaluation.setApplicationId(app.getId());
        evaluation.setJobId(app.getJobId());
        evaluation.setApplicantId(app.getApplicantId());
        evaluation.setEvaluatorId(moId);
        evaluation.setEvaluatorName(evaluatorName != null ? evaluatorName : moId);
        evaluation.setTechnicalScore(score(req, "technicalScore"));
        evaluation.setTeachingScore(score(req, "teachingScore"));
        evaluation.setCommunicationScore(score(req, "communicationScore"));
        evaluation.setAvailabilityScore(score(req, "availabilityScore"));
        evaluation.setResponsibilityScore(score(req, "responsibilityScore"));
        evaluation.setRecommendation(recommendation);
        evaluation.setStrengths(trim(req.getParameter("strengths")));
        evaluation.setConcerns(trim(req.getParameter("concerns")));
        evaluation.setInternalNotes(trim(req.getParameter("internalNotes")));
        storage.saveInterviewEvaluation(evaluation);
        timelineService.record(storage, app, job, moId, evaluatorName, "MO",
                ApplicationTimelineService.TYPE_EVALUATION_SAVED,
                "Interview evaluation saved",
                "Score " + evaluation.getTotalScore() + "/100, recommendation: " + evaluation.getRecommendationLabel(),
                app.getStatus(), app.getStatus());

        redirectToJobs(req, resp, app.getJobId(), "interview", null);
    }

    private Application findApplication(List<Application> apps, String applicationId) {
        for (Application app : apps) {
            if (applicationId.equals(app.getId())) {
                return app;
            }
        }
        return null;
    }

    private int score(HttpServletRequest req, String name) {
        try {
            int value = Integer.parseInt(trim(req.getParameter(name)));
            if (value < 1) return 1;
            if (value > 5) return 5;
            return value;
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    private void redirectToJobs(HttpServletRequest req, HttpServletResponse resp, String jobId, String view, String error)
            throws IOException {
        String returnBase = trim(req.getParameter("returnBase"));
        if (returnBase.isEmpty() || !returnBase.startsWith(req.getContextPath())) {
            returnBase = req.getContextPath() + "/mo/jobs";
        }
        String returnJobId = trim(req.getParameter("returnJobId"));
        if (jobId != null && !jobId.isEmpty()) {
            returnJobId = jobId;
        }
        String returnView = trim(req.getParameter("returnView"));
        if (returnView.isEmpty()) {
            returnView = view != null && !view.isEmpty() ? view : "interview";
        }

        StringBuilder url = new StringBuilder(returnBase).append("?");
        if (!returnJobId.isEmpty()) {
            url.append("jobId=").append(URLEncoder.encode(returnJobId, StandardCharsets.UTF_8)).append("&");
        }
        url.append("view=").append(URLEncoder.encode(returnView, StandardCharsets.UTF_8));
        if (error == null || error.isEmpty()) {
            url.append("&evaluationSaved=1");
        } else {
            url.append("&error=").append(URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
        MoJobsRedirectParams.appendExpandApp(url, returnView, trim(req.getParameter("applicationId")));
        resp.sendRedirect(url.toString());
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

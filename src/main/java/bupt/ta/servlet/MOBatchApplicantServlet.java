package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Batch operations for MO: mark PENDING as INTERVIEW, or send in-app interview notice to INTERVIEW applicants.
 */
public class MOBatchApplicantServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        String action = req.getParameter("action");
        String[] ids = req.getParameterValues("applicationId");
        if (ids == null || ids.length == 0) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=batch_empty");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Set<String> idSet = new HashSet<>(Arrays.asList(ids));

        if ("toInterview".equals(action)) {
            for (String appId : idSet) {
                Application target = findApp(storage, appId);
                if (target == null) continue;
                Job job = storage.getJobById(target.getJobId());
                if (job == null || !moId.equals(job.getPostedBy())) continue;
                if (!"PENDING".equals(target.getStatus())) continue;
                target.setStatus("INTERVIEW");
                storage.saveApplication(target);
            }
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?updated=1");
            return;
        }

        if ("sendNotice".equals(action)) {
            String time = trim(req.getParameter("interviewTime"));
            String location = trim(req.getParameter("interviewLocation"));
            String assessment = trim(req.getParameter("interviewAssessment"));
            for (String appId : idSet) {
                Application target = findApp(storage, appId);
                if (target == null) continue;
                Job job = storage.getJobById(target.getJobId());
                if (job == null || !moId.equals(job.getPostedBy())) continue;
                if (!"INTERVIEW".equals(target.getStatus())) continue;
                target.setInterviewTime(time);
                target.setInterviewLocation(location);
                target.setInterviewAssessment(assessment);
                storage.saveApplication(target);
            }
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?notice=1");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid_action");
    }

    private static Application findApp(DataStorage storage, String appId) throws IOException {
        return storage.loadApplications().stream()
                .filter(a -> a.getId().equals(appId))
                .findFirst()
                .orElse(null);
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }
}

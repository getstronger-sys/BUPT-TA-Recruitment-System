package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Toggle save/unsave for TA jobs.
 */
public class SaveJobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        String jobId = trim(req.getParameter("jobId"));
        String action = trim(req.getParameter("action"));
        String returnTo = trim(req.getParameter("returnTo"));

        if (jobId == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=invalid_job");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Job job = storage.getJobById(jobId);
        if (job == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=job_not_found");
            return;
        }

        boolean shouldSave = !"unsave".equalsIgnoreCase(action);
        storage.setJobSaved(userId, jobId, shouldSave);

        String fallback = "/ta/job?jobId=" + jobId;
        resp.sendRedirect(req.getContextPath() + safeTaPath(returnTo, fallback));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safeTaPath(String value, String fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        if (!value.startsWith("/ta/")) {
            return fallback;
        }
        if (value.contains("..") || value.contains("\r") || value.contains("\n")) {
            return fallback;
        }
        return value;
    }
}

package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Shows job + current profile/CV summary before POSTing to {@link ApplyJobServlet}.
 */
public class ApplyConfirmServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        String applicantId = (String) req.getSession().getAttribute("userId");

        if (jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=invalid_job");
            return;
        }
        jobId = jobId.trim();

        DataStorage storage = new DataStorage(getServletContext());
        Job job = storage.getJobById(jobId);
        if (job == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=job_not_found");
            return;
        }
        if (!"OPEN".equals(job.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=job_closed");
            return;
        }
        if (storage.hasApplied(jobId, applicantId)) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=already_applied");
            return;
        }

        TAProfile profile = storage.getProfileByUserId(applicantId);
        if (profile == null) {
            profile = new TAProfile(applicantId);
        }

        String returnPath = "/ta/apply-confirm?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8);
        String profileEditUrl = req.getContextPath() + "/ta/profile?returnUrl="
                + URLEncoder.encode(returnPath, StandardCharsets.UTF_8);

        req.setAttribute("job", job);
        req.setAttribute("profile", profile);
        req.setAttribute("profileEditUrl", profileEditUrl);
        req.getRequestDispatcher("/ta/apply-confirm.jsp").forward(req, resp);
    }
}

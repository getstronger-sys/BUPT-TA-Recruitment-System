package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.User;
import bupt.ta.service.StudentNotificationService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class ApplyJobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        String preferredRole = req.getParameter("preferredRole");
        String applicantId = (String) req.getSession().getAttribute("userId");
        String applicantName = (String) req.getSession().getAttribute("realName");
        if (applicantName == null) {
            User u = (User) req.getSession().getAttribute("user");
            applicantName = u != null ? u.getUsername() : "Unknown";
        }

        if (jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=invalid_job");
            return;
        }

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
        int taSlots = job.getTaSlots() > 0 ? job.getTaSlots() : 1;
        if (preferredRole == null || !preferredRole.matches("^TA-[1-9]\\d*$")) {
            resp.sendRedirect(req.getContextPath() + "/ta/apply-confirm?jobId=" + jobId + "&error=invalid_role");
            return;
        }
        int roleNo;
        try {
            roleNo = Integer.parseInt(preferredRole.substring(3));
        } catch (NumberFormatException ex) {
            resp.sendRedirect(req.getContextPath() + "/ta/apply-confirm?jobId=" + jobId + "&error=invalid_role");
            return;
        }
        if (roleNo <= 0 || roleNo > taSlots) {
            resp.sendRedirect(req.getContextPath() + "/ta/apply-confirm?jobId=" + jobId + "&error=invalid_role");
            return;
        }
        if (storage.hasApplied(jobId, applicantId)) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs?error=already_applied");
            return;
        }

        Application app = new Application();
        app.setJobId(jobId);
        app.setApplicantId(applicantId);
        app.setApplicantName(applicantName);
        app.setPreferredRole(preferredRole);
        storage.addApplication(app);
        StudentNotificationService.notifyApplicationSubmitted(storage, app, job);

        resp.sendRedirect(req.getContextPath() + "/ta/applications?success=1");
    }
}

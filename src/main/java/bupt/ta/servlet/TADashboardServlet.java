package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.TAProfile;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * TA home: summary counts and quick links (replaces bare redirect from login).
 */
public class TADashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        storage.syncJobStatusesWithDeadlines();

        TAProfile profile = storage.getOrCreateProfile(userId);

        List<Application> apps = storage.getApplicationsByApplicantId(userId);
        int pending = 0;
        int selected = 0;
        int other = 0;
        for (Application a : apps) {
            if ("PENDING".equals(a.getStatus())) {
                pending++;
            } else if ("SELECTED".equals(a.getStatus())) {
                selected++;
            } else {
                other++;
            }
        }

        long openJobs = storage.loadJobs().stream().filter(j -> "OPEN".equals(j.getStatus())).count();

        boolean hasCv = profile.getCvFilePath() != null && !profile.getCvFilePath().trim().isEmpty();
        boolean hasSkills = profile.getSkills() != null && !profile.getSkills().isEmpty();
        boolean hasStudentId = profile.getStudentId() != null && !profile.getStudentId().trim().isEmpty();
        boolean hasEmail = profile.getEmail() != null && !profile.getEmail().trim().isEmpty();
        if (!hasEmail) {
            try {
                User u = storage.findUserById(userId);
                if (u != null && u.getEmail() != null && !u.getEmail().trim().isEmpty()) {
                    hasEmail = true;
                }
            } catch (IOException ignored) {
            }
        }

        req.setAttribute("profile", profile);
        req.setAttribute("totalApplications", apps.size());
        req.setAttribute("pendingCount", pending);
        req.setAttribute("selectedCount", selected);
        req.setAttribute("otherApplicationsCount", other);
        req.setAttribute("openJobsCount", (int) openJobs);
        req.setAttribute("savedJobsCount", profile.getSavedJobIds().size());
        req.setAttribute("hasCv", hasCv);
        req.setAttribute("hasSkills", hasSkills);
        req.setAttribute("hasStudentId", hasStudentId);
        req.setAttribute("hasEmail", hasEmail);

        req.getRequestDispatcher("/ta/dashboard.jsp").forward(req, resp);
    }
}

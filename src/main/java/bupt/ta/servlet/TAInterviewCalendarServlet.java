package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.InterviewCalendarSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Downloads an interview appointment as an .ics calendar file for the logged-in TA.
 */
public class TAInterviewCalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        String applicationId = req.getParameter("applicationId");
        if (applicationId == null || applicationId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application ID is required.");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Application application = storage.getApplicationsByApplicantId(userId).stream()
                .filter(a -> applicationId.trim().equals(a.getId()))
                .findFirst()
                .orElse(null);
        if (application == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Application not found.");
            return;
        }
        if (application.getInterviewTime() == null || application.getInterviewTime().trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Interview time has not been posted yet.");
            return;
        }

        Job job = storage.getJobById(application.getJobId());
        final String calendarText;
        try {
            calendarText = InterviewCalendarSupport.buildCalendarFile(application, job);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        String filename = InterviewCalendarSupport.buildFilename(application, job);
        resp.setContentType("text/calendar; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename.replace("\"", "") + "\"");
        resp.getOutputStream().write(calendarText.getBytes(StandardCharsets.UTF_8));
    }
}

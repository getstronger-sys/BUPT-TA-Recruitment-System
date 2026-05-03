package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.service.ApplicationTimelineService;
import bupt.ta.service.InterviewBookingService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * TA view to book, change, or cancel a slot for one interview-stage application.
 */
public class TAInterviewBookingServlet extends HttpServlet {

    private final InterviewBookingService bookingService = new InterviewBookingService();
    private final ApplicationTimelineService timelineService = new ApplicationTimelineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String applicantId = (String) req.getSession().getAttribute("userId");
        String applicationId = trim(req.getParameter("applicationId"));
        if (applicationId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/applications?error=not_found");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Application application = storage.getApplicationsByApplicantId(applicantId).stream()
                .filter(a -> applicationId.equals(a.getId()))
                .findFirst()
                .orElse(null);
        if (application == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/applications?error=not_found");
            return;
        }

        Job job = storage.getJobById(application.getJobId());
        req.setAttribute("application", application);
        req.setAttribute("job", job);
        req.setAttribute("slotSummaries", bookingService.buildSlotSummaries(storage, application.getJobId()));
        req.getRequestDispatcher("/ta/interview-booking.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String applicantId = (String) req.getSession().getAttribute("userId");
        String applicationId = trim(req.getParameter("applicationId"));
        String action = trim(req.getParameter("action"));

        DataStorage storage = new DataStorage(getServletContext());
        InterviewBookingService.ActionResult result;
        if ("book".equals(action)) {
            result = bookingService.bookSlot(storage, applicantId, applicationId, trim(req.getParameter("slotId")));
        } else if ("cancel".equals(action)) {
            result = bookingService.cancelBooking(storage, applicantId, applicationId);
        } else {
            result = new InterviewBookingService.ActionResult(false, "Unknown booking action.");
        }

        String redirect = req.getContextPath() + "/ta/interview-booking?applicationId="
                + URLEncoder.encode(applicationId, StandardCharsets.UTF_8.name());
        if (result.isSuccess()) {
            Application application = storage.getApplicationsByApplicantId(applicantId).stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst()
                    .orElse(null);
            Job job = application != null ? storage.getJobById(application.getJobId()) : null;
            if ("book".equals(action)) {
                timelineService.record(storage, application, job, applicantId, application != null ? application.getApplicantName() : applicantId, "TA",
                        ApplicationTimelineService.TYPE_INTERVIEW_BOOKED,
                        "Interview slot booked",
                        application != null && application.getInterviewTime() != null ? application.getInterviewTime() : "Interview slot booked.",
                        application != null ? application.getStatus() : "", application != null ? application.getStatus() : "");
            } else if ("cancel".equals(action)) {
                timelineService.record(storage, application, job, applicantId, application != null ? application.getApplicantName() : applicantId, "TA",
                        ApplicationTimelineService.TYPE_INTERVIEW_CANCELLED,
                        "Interview booking cancelled",
                        "Applicant cancelled the booked interview slot.",
                        application != null ? application.getStatus() : "", application != null ? application.getStatus() : "");
            }
            redirect += "&saved=1";
        } else {
            redirect += "&error=" + URLEncoder.encode(result.getDetail(), StandardCharsets.UTF_8.name());
        }
        resp.sendRedirect(redirect);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

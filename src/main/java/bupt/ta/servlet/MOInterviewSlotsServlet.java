package bupt.ta.servlet;

import bupt.ta.service.InterviewBookingService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * MO management of interview slots for one posting.
 */
public class MOInterviewSlotsServlet extends HttpServlet {

    private final InterviewBookingService bookingService = new InterviewBookingService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        String jobId = trim(req.getParameter("jobId"));
        String action = trim(req.getParameter("action"));
        if (jobId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid_job");
            return;
        }

        InterviewBookingService.ActionResult result;
        if ("create".equals(action)) {
            result = bookingService.createSlot(
                    new bupt.ta.storage.DataStorage(getServletContext()),
                    moId,
                    jobId,
                    trim(req.getParameter("startsAt")),
                    trim(req.getParameter("durationMinutes")),
                    trim(req.getParameter("location")),
                    trim(req.getParameter("notes")),
                    trim(req.getParameter("capacity"))
            );
        } else if ("delete".equals(action)) {
            result = bookingService.deleteSlot(
                    new bupt.ta.storage.DataStorage(getServletContext()),
                    moId,
                    jobId,
                    trim(req.getParameter("slotId"))
            );
        } else {
            result = new InterviewBookingService.ActionResult(false, "Unknown slot action.");
        }

        String redirect = req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8.name());
        if (result.isSuccess()) {
            redirect += "&slotSaved=1";
        } else {
            redirect += "&slotError=" + URLEncoder.encode(result.getDetail(), StandardCharsets.UTF_8.name());
        }
        resp.sendRedirect(redirect);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

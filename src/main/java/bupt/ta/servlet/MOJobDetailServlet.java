package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.service.InterviewBookingService;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MO view of a full job posting (same fields as TA-facing detail, without apply flow).
 */
public class MOJobDetailServlet extends HttpServlet {

    private final InterviewBookingService bookingService = new InterviewBookingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        if (jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid_job");
            return;
        }

        String moId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        Job job = storage.getJobById(jobId.trim());
        if (job == null || !moId.equals(job.getPostedBy())) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=forbidden");
            return;
        }

        req.setAttribute("job", job);
        req.setAttribute("slotSummaries", bookingService.buildSlotSummaries(storage, job.getId()));
        req.setAttribute("moListPath", JobActivity.listPathFor(job));
        req.setAttribute("moPastJobsPage", JobActivity.isInactive(job));
        req.setAttribute("moJobsBase", req.getContextPath() + JobActivity.listPathFor(job));
        req.getRequestDispatcher("/mo/job-detail.jsp").forward(req, resp);
    }
}

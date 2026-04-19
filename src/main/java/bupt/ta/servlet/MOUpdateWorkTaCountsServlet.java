package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.model.WorkArrangementItem;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;
import bupt.ta.util.WorkArrangementSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MO updates structured work arrangements (and planned recruits) on an active posting;
 * recomputes derived workload text. Requires double acknowledgement on the form.
 */
public class MOUpdateWorkTaCountsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        String moId = (String) req.getSession().getAttribute("userId");
        if (jobId == null || jobId.trim().isEmpty() || moId == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid_job");
            return;
        }
        jobId = jobId.trim();

        DataStorage storage = new DataStorage(getServletContext());
        storage.syncJobStatusesWithDeadlines();
        Job job = storage.getJobById(jobId);
        if (job == null || !moId.equals(job.getPostedBy())) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=forbidden");
            return;
        }
        if (JobActivity.isInactive(job)) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=wa_job_inactive");
            return;
        }

        boolean ack1 = req.getParameter("waConfirmUnderstand") != null;
        boolean ack2 = req.getParameter("waConfirmAccurate") != null;
        if (!ack1 || !ack2) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=wa_confirm_required");
            return;
        }

        List<WorkArrangementItem> rows = WorkArrangementSupport.parseWorkRowsFromRequest(req);
        String validation = WorkArrangementSupport.validateWorkRowsForPosting(rows);
        if (validation != null) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=wa_validation");
            return;
        }

        int plannedRecruits = job.getTaSlots() > 0 ? job.getTaSlots() : 1;
        String plannedRaw = req.getParameter("plannedTaCount");
        if (plannedRaw != null && !plannedRaw.trim().isEmpty()) {
            try {
                plannedRecruits = Integer.parseInt(plannedRaw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (plannedRecruits < 1) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=planned_ta_invalid");
            return;
        }

        WorkArrangementSupport.applyDerivedFields(job, rows);
        job.setTaSlots(plannedRecruits);
        storage.saveJob(job);

        resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&workArrangementsUpdated=1");
    }
}

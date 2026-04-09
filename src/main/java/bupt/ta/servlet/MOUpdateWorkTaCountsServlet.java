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
 * MO may adjust TA headcount per work-arrangement row when viewing a posting;
 * recomputes summary text but keeps planned recruit count (taSlots) unchanged.
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
        Job job = storage.getJobById(jobId);
        if (job == null || !moId.equals(job.getPostedBy())) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=forbidden");
            return;
        }

        List<WorkArrangementItem> rows = job.getWorkArrangements();
        if (rows == null || rows.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8));
            return;
        }

        String[] counts = req.getParameterValues("waTaCount");
        if (counts == null || counts.length != rows.size()) {
            resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=wa_count_mismatch");
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

        for (int i = 0; i < rows.size(); i++) {
            int n = 0;
            try {
                n = Integer.parseInt(counts[i] != null ? counts[i].trim() : "");
            } catch (NumberFormatException ignored) {
            }
            if (n < 1) {
                resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&error=wa_ta_invalid");
                return;
            }
            rows.get(i).setTaCount(n);
        }

        WorkArrangementSupport.applyDerivedFields(job, rows);
        job.setTaSlots(plannedRecruits);
        storage.saveJob(job);

        String path = JobActivity.listPathFor(job);
        resp.sendRedirect(req.getContextPath() + "/mo/job?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&taCountsUpdated=1");
    }
}

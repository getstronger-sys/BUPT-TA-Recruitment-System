package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.JobActivity;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CloseJobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jobId = req.getParameter("jobId");
        String action = req.getParameter("action"); // close or reopen
        String moId = (String) req.getSession().getAttribute("userId");

        if (jobId == null || jobId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Job job = storage.getJobById(jobId);
        if (job == null || !moId.equals(job.getPostedBy())) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=forbidden");
            return;
        }

        if ("close".equalsIgnoreCase(action)) {
            job.setStatus("CLOSED");
        } else if ("reopen".equalsIgnoreCase(action)) {
            job.setStatus("OPEN");
        }
        storage.saveJob(job);
        String enc = URLEncoder.encode(jobId.trim(), StandardCharsets.UTF_8);
        String path = JobActivity.listPathFor(job);
        resp.sendRedirect(req.getContextPath() + path + "?updated=1&jobId=" + enc + "&view=pending");
    }
}

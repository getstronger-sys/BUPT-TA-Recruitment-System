package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class SelectApplicantServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String appId = req.getParameter("applicationId");
        String action = req.getParameter("action");  // select or reject
        String notes = req.getParameter("notes");
        String moId = (String) req.getSession().getAttribute("userId");

        if (appId == null || appId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=invalid");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        List<Application> apps = storage.loadApplications();
        Application target = apps.stream().filter(a -> a.getId().equals(appId)).findFirst().orElse(null);

        if (target == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=not_found");
            return;
        }

        Job job = storage.getJobById(target.getJobId());
        if (job == null || !moId.equals(job.getPostedBy())) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=forbidden");
            return;
        }

        if ("select".equalsIgnoreCase(action)) {
            target.setStatus("SELECTED");
        } else if ("reject".equalsIgnoreCase(action)) {
            target.setStatus("REJECTED");
        }
        target.setNotes(notes != null ? notes.trim() : "");
        storage.saveApplication(target);

        resp.sendRedirect(req.getContextPath() + "/mo/jobs?updated=1");
    }
}

package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class WithdrawApplicationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String appId = req.getParameter("applicationId");
        String applicantId = (String) req.getSession().getAttribute("userId");

        if (appId == null || appId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/applications?error=invalid");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        List<Application> apps = storage.loadApplications();
        Application target = apps.stream()
                .filter(a -> a.getId().equals(appId) && applicantId.equals(a.getApplicantId()))
                .findFirst().orElse(null);

        if (target == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/applications?error=not_found");
            return;
        }
        if (!"PENDING".equals(target.getStatus()) && !"INTERVIEW".equals(target.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/ta/applications?error=already_processed");
            return;
        }

        target.setStatus("WITHDRAWN");
        storage.saveApplication(target);
        resp.sendRedirect(req.getContextPath() + "/ta/applications?withdrawn=1");
    }
}

package bupt.ta.servlet;

import bupt.ta.model.AdminSettings;
import bupt.ta.service.AdminService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin home: overall recruitment summary + workload-rule settings.
 */
public class AdminDashboardServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();

        req.setAttribute("adminSettings", settings);
        req.setAttribute("summary", adminService.buildDashboardSummary(storage, settings));
        req.setAttribute("monitoring", adminService.buildMonitoringReport(storage, settings));
        req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();

        String limitRaw = req.getParameter("maxSelectedJobsPerTa");
        int limit = settings.getMaxSelectedJobsPerTa();
        String error = null;
        try {
            limit = limitRaw != null && !limitRaw.trim().isEmpty() ? Integer.parseInt(limitRaw.trim()) : 0;
            if (limit < 0) {
                error = "Workload limit cannot be negative.";
            }
        } catch (NumberFormatException e) {
            error = "Workload limit must be a valid number.";
        }

        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("adminSettings", settings);
            req.setAttribute("summary", adminService.buildDashboardSummary(storage, settings));
            req.setAttribute("monitoring", adminService.buildMonitoringReport(storage, settings));
            req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
            return;
        }

        settings.setMaxSelectedJobsPerTa(limit);
        settings.setAutoClosePendingWhenLimitReached(req.getParameter("autoClosePendingWhenLimitReached") != null);
        storage.saveAdminSettings(settings);

        int autoClosed = adminService.enforceWorkloadLimitGlobally(storage, settings);
        String redirect = req.getContextPath() + "/admin/dashboard?saved=1";
        if (autoClosed > 0) {
            redirect += "&autoClosed=" + autoClosed;
        }
        resp.sendRedirect(redirect);
    }
}

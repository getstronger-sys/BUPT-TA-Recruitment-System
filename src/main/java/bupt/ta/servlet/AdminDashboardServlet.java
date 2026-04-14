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
        String hoursRaw = req.getParameter("maxWorkloadHoursPerTa");
        int limit = settings.getMaxSelectedJobsPerTa();
        double hourCap = settings.getMaxWorkloadHoursPerTa();
        String error = null;
        try {
            limit = limitRaw != null && !limitRaw.trim().isEmpty() ? Integer.parseInt(limitRaw.trim()) : 0;
            if (limit < 0) {
                error = "Maximum selected jobs per TA cannot be negative.";
            }
        } catch (NumberFormatException e) {
            error = "Maximum selected jobs per TA must be a valid integer.";
        }
        try {
            if (error == null) {
                hourCap = hoursRaw != null && !hoursRaw.trim().isEmpty()
                        ? Double.parseDouble(hoursRaw.trim())
                        : 0.0;
                if (hourCap < 0 || Double.isNaN(hourCap) || Double.isInfinite(hourCap)) {
                    error = "Maximum workload hours must be zero or a positive number.";
                }
            }
        } catch (NumberFormatException e) {
            error = "Maximum workload hours must be a valid number.";
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
        settings.setMaxWorkloadHoursPerTa(hourCap);
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

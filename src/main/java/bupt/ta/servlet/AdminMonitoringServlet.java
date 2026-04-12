package bupt.ta.servlet;

import bupt.ta.model.AdminSettings;
import bupt.ta.service.AdminService;
import bupt.ta.service.NotificationReminderService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin exception view: monitor status/data anomalies across the system.
 */
public class AdminMonitoringServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final NotificationReminderService reminderService = new NotificationReminderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();

        req.setAttribute("adminSettings", settings);
        req.setAttribute("monitoring", adminService.buildMonitoringReport(storage, settings));
        req.setAttribute("reminderPreview", reminderService.buildPreview(storage));
        req.getRequestDispatcher("/admin/monitoring.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (!"sendUnreadReminders".equals(action)) {
            resp.sendRedirect(req.getContextPath() + "/admin/monitoring");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        NotificationReminderService.ReminderResult result = reminderService.sendUnreadReminders(storage);
        StringBuilder redirect = new StringBuilder(req.getContextPath())
                .append("/admin/monitoring?remindDone=1")
                .append("&remindConfigured=").append(result.isEmailConfigured() ? "1" : "0")
                .append("&remindAttempted=").append(result.getAttemptedUsers())
                .append("&remindSent=").append(result.getEmailedUsers())
                .append("&remindSkipped=").append(result.getSkippedUsers());
        resp.sendRedirect(redirect.toString());
    }
}

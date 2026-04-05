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
 * Admin exception view: monitor status/data anomalies across the system.
 */
public class AdminMonitoringServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();

        req.setAttribute("adminSettings", settings);
        req.setAttribute("monitoring", adminService.buildMonitoringReport(storage, settings));
        req.getRequestDispatcher("/admin/monitoring.jsp").forward(req, resp);
    }
}

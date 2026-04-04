package bupt.ta.servlet;

import bupt.ta.model.AdminSettings;
import bupt.ta.service.AdminService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * Admin: Check TA's overall workload - number of selected jobs per TA.
 */
public class AdminWorkloadServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();
        List<AdminService.WorkloadRow> workloadRows = adminService.buildWorkloadRows(storage, settings);
        double avgWorkload = workloadRows.isEmpty()
                ? 0
                : workloadRows.stream().mapToInt(AdminService.WorkloadRow::getSelectedCount).average().orElse(0);

        req.setAttribute("adminSettings", settings);
        req.setAttribute("workloadRows", workloadRows);
        req.setAttribute("avgWorkload", avgWorkload);
        req.getRequestDispatcher("/admin/workload-v2.jsp").forward(req, resp);
    }
}

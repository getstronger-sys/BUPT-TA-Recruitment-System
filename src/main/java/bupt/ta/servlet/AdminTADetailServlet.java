package bupt.ta.servlet;

import bupt.ta.service.AdminService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin read-only detail page for one TA.
 */
public class AdminTADetailServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        String userId = req.getParameter("userId");
        AdminService.TADetailReport report = adminService.buildTADetailReport(storage, userId);
        if (report == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?error=invalid_ta");
            return;
        }

        req.setAttribute("report", report);
        req.getRequestDispatcher("/admin/ta-detail.jsp").forward(req, resp);
    }
}

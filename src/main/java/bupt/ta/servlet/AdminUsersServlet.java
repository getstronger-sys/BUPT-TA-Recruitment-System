package bupt.ta.servlet;

import bupt.ta.service.AdminService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin directory of platform users with cross-role summary metrics.
 */
public class AdminUsersServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        String role = req.getParameter("role");
        String query = req.getParameter("q");

        req.setAttribute("directory", adminService.buildUserDirectoryReport(storage, role, query));
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }
}

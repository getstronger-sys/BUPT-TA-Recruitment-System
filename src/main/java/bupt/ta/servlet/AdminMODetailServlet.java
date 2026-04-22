package bupt.ta.servlet;

import bupt.ta.model.AssignedModule;
import bupt.ta.service.AdminService;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin read-only detail page for one module organiser.
 */
public class AdminMODetailServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        String userId = req.getParameter("userId");
        AdminService.MODetailReport report = adminService.buildMODetailReport(storage, userId);
        if (report == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?error=invalid_mo");
            return;
        }

        req.setAttribute("report", report);
        req.setAttribute("assignedModules", storage.loadAssignedModulesForMo(userId));
        req.getRequestDispatcher("/admin/mo-detail.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        String userId = trim(req.getParameter("userId"));
        AdminService.MODetailReport report = adminService.buildMODetailReport(storage, userId);
        if (report == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?error=invalid_mo");
            return;
        }
        String raw = req.getParameter("assignedModulesText");
        try {
            List<AssignedModule> modules = parseAssignedModules(raw);
            storage.saveAssignedModulesForMo(userId, modules);
            resp.sendRedirect(req.getContextPath() + "/admin/mo-detail?userId="
                    + URLEncoder.encode(userId, StandardCharsets.UTF_8.name())
                    + "&assignedUpdated=1");
        } catch (IllegalArgumentException ex) {
            req.setAttribute("report", report);
            req.setAttribute("assignedModules", storage.loadAssignedModulesForMo(userId));
            req.setAttribute("assignError", ex.getMessage());
            req.getRequestDispatcher("/admin/mo-detail.jsp").forward(req, resp);
        }
    }

    private static List<AssignedModule> parseAssignedModules(String raw) {
        List<AssignedModule> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            String row = line != null ? line.trim() : "";
            if (row.isEmpty()) {
                continue;
            }
            String[] parts = row.split("\\|", 2);
            String code = parts[0].trim().toUpperCase();
            if (code.isEmpty()) {
                continue;
            }
            if (!code.matches("[A-Z0-9_-]{3,20}")) {
                throw new IllegalArgumentException("Invalid module code: " + code + ". Use letters/numbers only.");
            }
            String name = parts.length > 1 ? parts[1].trim() : "";
            result.add(new AssignedModule(code, name));
        }
        return deduplicateByCode(result);
    }

    private static List<AssignedModule> deduplicateByCode(List<AssignedModule> modules) {
        List<AssignedModule> out = new ArrayList<>();
        for (AssignedModule m : modules) {
            boolean exists = out.stream().anyMatch(x -> x.getModuleCode().equalsIgnoreCase(m.getModuleCode()));
            if (!exists) {
                out.add(m);
            }
        }
        return out;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

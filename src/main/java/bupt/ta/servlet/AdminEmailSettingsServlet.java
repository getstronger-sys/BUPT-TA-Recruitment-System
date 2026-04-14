package bupt.ta.servlet;

import bupt.ta.model.AdminSettings;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin page for configuring SMTP settings used by email notifications.
 */
public class AdminEmailSettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();
        req.setAttribute("adminSettings", settings);
        req.getRequestDispatcher("/admin/email-settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        AdminSettings settings = storage.loadAdminSettings();

        String error = applyMailSettingsFromRequest(settings, req);
        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("adminSettings", settings);
            req.getRequestDispatcher("/admin/email-settings.jsp").forward(req, resp);
            return;
        }
        storage.saveAdminSettings(settings);
        resp.sendRedirect(req.getContextPath() + "/admin/email?saved=1");
    }

    private static String applyMailSettingsFromRequest(AdminSettings settings, HttpServletRequest req) {
        if (settings == null) {
            return "Admin settings not available.";
        }
        String host = trim(req.getParameter("mailHost"));
        String portRaw = trim(req.getParameter("mailPort"));
        String username = trim(req.getParameter("mailUsername"));
        String password = req.getParameter("mailPassword"); // keep spaces as-is
        String from = trim(req.getParameter("mailFrom"));
        String appBaseUrl = trim(req.getParameter("mailAppBaseUrl"));

        int port = settings.getMailPort();
        if (!portRaw.isEmpty()) {
            try {
                port = Integer.parseInt(portRaw);
                if (port <= 0 || port > 65535) {
                    return "SMTP port must be between 1 and 65535.";
                }
            } catch (NumberFormatException e) {
                return "SMTP port must be a valid integer.";
            }
        }

        settings.setMailEnabled(req.getParameter("mailEnabled") != null);
        settings.setMailAuth(req.getParameter("mailAuth") != null);
        settings.setMailStartTls(req.getParameter("mailStartTls") != null);
        settings.setMailSsl(req.getParameter("mailSsl") != null);

        settings.setMailHost(host);
        settings.setMailPort(port);
        settings.setMailUsername(username);
        if (password != null && !password.isEmpty()) {
            settings.setMailPassword(password);
        }
        settings.setMailFrom(from);
        settings.setMailAppBaseUrl(appBaseUrl);

        if (settings.isMailEnabled()) {
            if (isBlank(settings.getMailHost())) {
                return "SMTP host is required when email delivery is enabled.";
            }
            if (isBlank(settings.getMailFrom())) {
                return "Sender address (from) is required when email delivery is enabled.";
            }
        }
        return null;
    }

    private static String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}


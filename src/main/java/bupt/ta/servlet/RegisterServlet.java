package bupt.ta.servlet;

import bupt.ta.model.TAProfile;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterServlet extends HttpServlet {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String role = trim(req.getParameter("role"));
        String email = trim(req.getParameter("email"));
        String studentId = trim(req.getParameter("studentId"));
        String realName = trim(req.getParameter("realName"));
        String error = null;

        if (username.isEmpty()) {
            error = "Username is required.";
        } else if (!"TA".equals(role) && !"MO".equals(role)) {
            error = "Please select a valid role (TA or MO).";
        } else if ("TA".equals(role) && studentId.isEmpty()) {
            error = "Student ID is required for applicant accounts.";
        } else if (email.isEmpty()) {
            error = "Email is required.";
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            error = "Please enter a valid email address.";
        } else if (password == null || password.length() < 4) {
            error = "Password must be at least 4 characters.";
        } else if (!password.equals(confirmPassword)) {
            error = "Passwords do not match.";
        } else {
            DataStorage storage = new DataStorage(getServletContext());
            if (storage.findByUsername(username) != null) {
                error = "Username already exists.";
            } else if (storage.findByEmail(email) != null) {
                error = "Email already exists.";
            } else if ("TA".equals(role) && storage.findProfileByStudentId(studentId) != null) {
                error = "Student ID already exists.";
            } else {
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(role);
                user.setEmail(email);
                user.setStudentId(studentId);
                user.setRealName(realName.isEmpty() ? username : realName);
                user = storage.addUser(user);

                if ("TA".equals(role)) {
                    TAProfile profile = new TAProfile(user.getId());
                    profile.setStudentId(studentId);
                    storage.saveProfile(profile);
                }

                resp.sendRedirect(req.getContextPath() + "/index.jsp?registered=1");
                return;
            }
        }
        req.setAttribute("username", username);
        req.setAttribute("role", role);
        req.setAttribute("email", email);
        req.setAttribute("studentId", studentId);
        req.setAttribute("realName", realName);
        req.setAttribute("error", error);
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

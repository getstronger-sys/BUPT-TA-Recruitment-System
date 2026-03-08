package bupt.ta.servlet;

import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String role = req.getParameter("role");
        String email = req.getParameter("email");
        String realName = req.getParameter("realName");
        String error = null;

        if (username == null || username.trim().isEmpty()) {
            error = "Username is required.";
        } else if (password == null || password.length() < 4) {
            error = "Password must be at least 4 characters.";
        } else if (!password.equals(confirmPassword)) {
            error = "Passwords do not match.";
        } else if (role == null || (!role.equals("TA") && !role.equals("MO"))) {
            error = "Please select a valid role (TA or MO).";
        } else {
            DataStorage storage = new DataStorage(getServletContext());
            if (storage.findByUsername(username.trim()) != null) {
                error = "Username already exists.";
            } else {
                User user = new User();
                user.setUsername(username.trim());
                user.setPassword(password);
                user.setRole(role);
                user.setEmail(email != null ? email.trim() : "");
                user.setRealName(realName != null ? realName.trim() : username);
                storage.addUser(user);
                resp.sendRedirect(req.getContextPath() + "/index.jsp?registered=1");
                return;
            }
        }
        req.setAttribute("error", error);
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }
}

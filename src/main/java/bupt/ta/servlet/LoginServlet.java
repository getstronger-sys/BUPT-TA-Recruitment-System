package bupt.ta.servlet;

import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String error = null;

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            error = "Please enter username and password.";
        } else {
            DataStorage storage = new DataStorage(getServletContext());
            User user = storage.findByUsername(username.trim());
            if (user == null || !user.getPassword().equals(password)) {
                error = "Invalid username or password.";
            } else {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setAttribute("realName", user.getRealName());
                session.setAttribute("justLoggedIn", Boolean.TRUE);

                String redirect = req.getContextPath() + "/dashboard.jsp";
                resp.sendRedirect(redirect);
                return;
            }
        }
        req.setAttribute("error", error);
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}

package bupt.ta.filter;

import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.RememberMeCookie;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Restores HttpSession from persistent remember-me cookie when the browser session cookie is gone.
 */
public class RememberMeFilter implements Filter {

    private static void populateSession(HttpSession session, User user) {
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setAttribute("realName", user.getRealName());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            chain.doFilter(req, resp);
            return;
        }

        String raw = RememberMeCookie.readRawToken(request);
        if (raw == null) {
            chain.doFilter(req, resp);
            return;
        }

        try {
            DataStorage storage = new DataStorage(request.getServletContext());
            User user = storage.validateRememberMeToken(raw);
            if (user == null) {
                storage.revokeRememberMeToken(raw);
                RememberMeCookie.clear(response, request.getContextPath(), request.isSecure());
                chain.doFilter(req, resp);
                return;
            }
            HttpSession s = request.getSession(true);
            populateSession(s, user);
        } catch (IOException e) {
            throw new ServletException(e);
        }

        chain.doFilter(req, resp);
    }
}

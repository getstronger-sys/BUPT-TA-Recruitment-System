package bupt.ta.filter;

import bupt.ta.util.CsrfTokenManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Provides synchronizer-token CSRF protection for state-changing requests.
 */
public class CsrfFilter implements Filter {

    private static final Set<String> SAFE_METHODS = new HashSet<>(Arrays.asList("GET", "HEAD", "OPTIONS", "TRACE"));

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String path = getPath(request);
        if (isStaticResource(path)) {
            chain.doFilter(req, resp);
            return;
        }

        String method = request.getMethod() != null
                ? request.getMethod().toUpperCase(Locale.ROOT)
                : "GET";

        if (SAFE_METHODS.contains(method)) {
            HttpSession session = request.getSession(true);
            CsrfTokenManager.getOrCreateToken(session);
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            reject(response);
            return;
        }

        String expectedToken = (String) session.getAttribute(CsrfTokenManager.SESSION_ATTRIBUTE);
        String providedToken = trimToNull(request.getParameter("csrfToken"));
        if (providedToken == null) {
            providedToken = trimToNull(request.getHeader("X-CSRF-Token"));
        }
        if (providedToken == null && isMultipartRequest(request)) {
            providedToken = trimToNull(readMultipartToken(request));
        }

        if (!CsrfTokenManager.isValid(expectedToken, providedToken)) {
            reject(response);
            return;
        }

        chain.doFilter(req, resp);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
    }

    private String getPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
    }

    private boolean isStaticResource(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return path.startsWith("/css/")
                || path.startsWith("/images/")
                || path.startsWith("/js/")
                || path.startsWith("/fonts/")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".svg")
                || path.endsWith(".ico")
                || path.endsWith(".woff")
                || path.endsWith(".woff2");
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private String readMultipartToken(HttpServletRequest request) {
        try {
            Part tokenPart = request.getPart("csrfToken");
            if (tokenPart == null) {
                return null;
            }
            try (InputStream in = tokenPart.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

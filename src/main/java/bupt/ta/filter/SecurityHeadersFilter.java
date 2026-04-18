package bupt.ta.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Applies a baseline set of security response headers.
 */
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; "
                        + "base-uri 'self'; "
                        + "frame-ancestors 'none'; "
                        + "object-src 'none'; "
                        + "form-action 'self'; "
                        + "img-src 'self' data: https:; "
                        + "font-src 'self' data:; "
                        + "style-src 'self' 'unsafe-inline'; "
                        + "script-src 'self' 'unsafe-inline'"
        );
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        chain.doFilter(req, resp);
    }
}

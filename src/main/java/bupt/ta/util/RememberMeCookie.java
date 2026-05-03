package bupt.ta.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Persistent cookie for "remember me" login (path matches web app context).
 */
public final class RememberMeCookie {

    public static final String NAME = "TA_REMEMBER";
    /** Must match {@link bupt.ta.storage.DataStorage} remember-me TTL. */
    public static final int MAX_AGE_SECONDS = 30 * 24 * 60 * 60;

    private RememberMeCookie() {}

    public static String readRawToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (NAME.equals(c.getName()) && c.getValue() != null && !c.getValue().trim().isEmpty()) {
                return c.getValue().trim();
            }
        }
        return null;
    }

    public static void setToken(HttpServletResponse response, String contextPath, boolean secure, String rawToken) {
        Cookie cookie = new Cookie(NAME, rawToken);
        cookie.setPath(cookiePath(contextPath));
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    public static void clear(HttpServletResponse response, String contextPath, boolean secure) {
        Cookie cookie = new Cookie(NAME, "");
        cookie.setPath(cookiePath(contextPath));
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    private static String cookiePath(String contextPath) {
        if (contextPath == null || contextPath.isEmpty()) {
            return "/";
        }
        return contextPath;
    }
}

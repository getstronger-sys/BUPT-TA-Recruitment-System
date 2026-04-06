package bupt.ta.servlet;

import bupt.ta.model.SiteNotification;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TA in-app messages: paginated list, filters, detail view, read / unread toggles.
 */
public class TAMessagesServlet extends HttpServlet {

    /** Fixed page size (industry-style inbox: scroll inside list, paginate by 10). */
    private static final int PAGE_SIZE = 10;

    static String normalizeFilter(String raw) {
        if ("unread".equalsIgnoreCase(raw)) {
            return "unread";
        }
        if ("read".equalsIgnoreCase(raw)) {
            return "read";
        }
        return "all";
    }

    private static int parsePositiveInt(String raw, int defaultVal) {
        if (raw == null || raw.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v < 1 ? defaultVal : v;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void appendMessagesQuery(StringBuilder b, int page, String filter) {
        b.append("?page=").append(page);
        if (!"all".equals(filter)) {
            b.append("&box=").append(encode(filter));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        req.setAttribute("taNavActive", "messages");

        String detailId = req.getParameter("id");
        if (detailId != null && !detailId.trim().isEmpty()) {
            SiteNotification detail = storage.getSiteNotificationByIdForUser(detailId.trim(), userId);
            if (detail == null) {
                resp.sendRedirect(req.getContextPath() + "/ta/messages");
                return;
            }
            String noAuto = req.getParameter("noAutoRead");
            boolean noAutoRead = "1".equals(noAuto) || "true".equalsIgnoreCase(noAuto);
            if (!noAutoRead) {
                storage.markSiteNotificationRead(detail.getId(), userId);
                detail.setRead(true);
            }
            List<SiteNotification> allForCounts = storage.getSiteNotificationsForUser(userId);
            int unreadCount = storage.countUnreadSiteNotificationsForUser(userId);
            req.setAttribute("notificationsAllTotal", allForCounts.size());
            req.setAttribute("notificationsUnreadTotal", unreadCount);
            req.setAttribute("notificationsReadTotal", allForCounts.size() - unreadCount);
            req.setAttribute("detailNotification", detail);
            req.setAttribute("listPageForBack", parsePositiveInt(req.getParameter("fromPage"), 1));
            req.setAttribute("messagesFilter", normalizeFilter(req.getParameter("box")));
            req.getRequestDispatcher("/ta/messages.jsp").forward(req, resp);
            return;
        }

        List<SiteNotification> all = storage.getSiteNotificationsForUser(userId);
        int unreadTotal = storage.countUnreadSiteNotificationsForUser(userId);
        int readTotal = all.size() - unreadTotal;

        String filter = normalizeFilter(req.getParameter("box"));
        List<SiteNotification> filtered = all.stream()
                .filter(n -> {
                    if ("unread".equals(filter)) {
                        return !n.isRead();
                    }
                    if ("read".equals(filter)) {
                        return n.isRead();
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filtered.size();
        int totalPages = total == 0 ? 1 : (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int page = parsePositiveInt(req.getParameter("page"), 1);
        if (page > totalPages) {
            page = totalPages;
        }
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<SiteNotification> slice = from >= total ? new ArrayList<>() : filtered.subList(from, to);

        req.setAttribute("notifications", slice);
        req.setAttribute("notificationsTotal", total);
        req.setAttribute("notificationsAllTotal", all.size());
        req.setAttribute("notificationsUnreadTotal", unreadTotal);
        req.setAttribute("notificationsReadTotal", readTotal);
        req.setAttribute("messagesPage", page);
        req.setAttribute("messagesTotalPages", totalPages);
        req.setAttribute("messagesFilter", filter);
        req.getRequestDispatcher("/ta/messages.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String userId = (String) req.getSession().getAttribute("userId");
        String action = req.getParameter("action");
        if (action != null) {
            action = action.trim();
        }
        DataStorage storage = new DataStorage(getServletContext());
        int returnPage = parsePositiveInt(req.getParameter("page"), 1);
        String filter = normalizeFilter(req.getParameter("box"));

        if ("markAllRead".equals(action)) {
            storage.markAllSiteNotificationsReadForUser(userId);
        } else if ("markSelectedRead".equals(action)) {
            String[] ids = req.getParameterValues("notificationId");
            if (ids != null && ids.length > 0) {
                storage.markSiteNotificationsReadForUser(userId, Arrays.asList(ids));
            }
        } else if ("markSelectedUnread".equals(action)) {
            String[] ids = req.getParameterValues("notificationId");
            if (ids != null && ids.length > 0) {
                storage.markSiteNotificationsUnreadForUser(userId, Arrays.asList(ids));
            }
        } else if ("markRead".equals(action)) {
            String nid = req.getParameter("notificationId");
            if (nid != null && !nid.trim().isEmpty()) {
                storage.markSiteNotificationRead(nid.trim(), userId);
            }
            if ("1".equals(req.getParameter("stayOnDetail")) && nid != null && !nid.trim().isEmpty()) {
                StringBuilder loc = new StringBuilder(req.getContextPath()).append("/ta/messages?id=").append(encode(nid.trim()));
                int fp = parsePositiveInt(req.getParameter("fromPage"), parsePositiveInt(req.getParameter("page"), 1));
                loc.append("&fromPage=").append(fp);
                if (!"all".equals(filter)) {
                    loc.append("&box=").append(encode(filter));
                }
                resp.sendRedirect(loc.toString());
                return;
            }
        }

        StringBuilder loc = new StringBuilder(req.getContextPath()).append("/ta/messages");
        appendMessagesQuery(loc, returnPage, filter);
        resp.sendRedirect(loc.toString());
    }
}

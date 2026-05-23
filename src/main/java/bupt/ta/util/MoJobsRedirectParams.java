package bupt.ta.util;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/** Query parameters for MO jobs redirects (e.g. interview fold expand state). */
public final class MoJobsRedirectParams {

    private MoJobsRedirectParams() {}

    /** Re-append sort/filter query params from the current request (GET or POST hidden fields). */
    public static void appendListControls(StringBuilder url, HttpServletRequest req) {
        if (req == null) {
            return;
        }
        String view = req.getParameter("view");
        if (view == null || view.isEmpty()) {
            Object viewAttr = req.getAttribute("moJobsView");
            if (viewAttr != null) {
                view = viewAttr.toString();
            }
        }
        if (!MoApplicantListControls.usesListToolbar(view)) {
            return;
        }
        String qs = MoApplicantListControls.fromRequest(req).toQueryString(view);
        if (qs == null || qs.isEmpty()) {
            return;
        }
        url.append('&').append(qs);
    }

    public static void appendExpandApp(StringBuilder url, String view, String expandApp) {
        if (expandApp == null || expandApp.isEmpty()) {
            return;
        }
        if (!"pending".equals(view) && !"interview".equals(view) && !"waitlist".equals(view) && !"outcome".equals(view)) {
            return;
        }
        url.append("&expandApp=").append(URLEncoder.encode(expandApp.trim(), StandardCharsets.UTF_8));
    }

    public static String joinExpandApps(Collection<String> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return "";
        }
        return applicationIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}

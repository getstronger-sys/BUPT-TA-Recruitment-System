package bupt.ta.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/** Query parameters for MO jobs redirects (e.g. interview fold expand state). */
public final class MoJobsRedirectParams {

    private MoJobsRedirectParams() {}

    public static void appendExpandApp(StringBuilder url, String view, String expandApp) {
        if (expandApp == null || expandApp.isEmpty() || !"interview".equals(view)) {
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

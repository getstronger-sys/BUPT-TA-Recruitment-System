package bupt.ta.model;

/**
 * Admin-managed recruitment rules.
 */
public class AdminSettings {
    /** 0 means no hard limit (job-count mode only). */
    private int maxSelectedJobsPerTa = 2;
    /**
     * 0 = do not use hour-based cap (fall back to {@link #maxSelectedJobsPerTa} when &gt; 0).
     * When &gt; 0, total estimated hours from structured work arrangements on selected posts is capped.
     */
    private double maxWorkloadHoursPerTa = 0.0;
    private boolean autoClosePendingWhenLimitReached = true;

    // ---- Optional SMTP email settings (admin-managed) ----
    private String mailHost = "";
    private int mailPort = 587;
    private String mailUsername = "";
    private String mailPassword = "";
    private String mailFrom = "";
    private boolean mailEnabled = true;
    private boolean mailAuth = true;
    private boolean mailStartTls = true;
    private boolean mailSsl = false;
    private String mailAppBaseUrl = "";

    public int getMaxSelectedJobsPerTa() {
        return maxSelectedJobsPerTa;
    }

    public void setMaxSelectedJobsPerTa(int maxSelectedJobsPerTa) {
        this.maxSelectedJobsPerTa = Math.max(0, maxSelectedJobsPerTa);
    }

    public double getMaxWorkloadHoursPerTa() {
        return maxWorkloadHoursPerTa;
    }

    public void setMaxWorkloadHoursPerTa(double maxWorkloadHoursPerTa) {
        this.maxWorkloadHoursPerTa = maxWorkloadHoursPerTa >= 0 ? maxWorkloadHoursPerTa : 0.0;
    }

    /** True when hour-based recruitment cap is active (takes precedence over job count). */
    public boolean usesHourWorkloadLimit() {
        return maxWorkloadHoursPerTa > 0;
    }

    public boolean isAutoClosePendingWhenLimitReached() {
        return autoClosePendingWhenLimitReached;
    }

    public void setAutoClosePendingWhenLimitReached(boolean autoClosePendingWhenLimitReached) {
        this.autoClosePendingWhenLimitReached = autoClosePendingWhenLimitReached;
    }

    public boolean hasWorkloadLimit() {
        return usesHourWorkloadLimit() || maxSelectedJobsPerTa > 0;
    }

    public String getMailHost() { return mailHost; }
    public void setMailHost(String mailHost) { this.mailHost = mailHost != null ? mailHost.trim() : ""; }

    public int getMailPort() { return mailPort; }
    public void setMailPort(int mailPort) { this.mailPort = mailPort > 0 ? mailPort : 587; }

    public String getMailUsername() { return mailUsername; }
    public void setMailUsername(String mailUsername) { this.mailUsername = mailUsername != null ? mailUsername.trim() : ""; }

    /** Stored as plain text in admin settings (sufficient for a course project; use secrets manager in production). */
    public String getMailPassword() { return mailPassword; }
    public void setMailPassword(String mailPassword) { this.mailPassword = mailPassword != null ? mailPassword : ""; }

    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String mailFrom) { this.mailFrom = mailFrom != null ? mailFrom.trim() : ""; }

    public boolean isMailEnabled() { return mailEnabled; }
    public void setMailEnabled(boolean mailEnabled) { this.mailEnabled = mailEnabled; }

    public boolean isMailAuth() { return mailAuth; }
    public void setMailAuth(boolean mailAuth) { this.mailAuth = mailAuth; }

    public boolean isMailStartTls() { return mailStartTls; }
    public void setMailStartTls(boolean mailStartTls) { this.mailStartTls = mailStartTls; }

    public boolean isMailSsl() { return mailSsl; }
    public void setMailSsl(boolean mailSsl) { this.mailSsl = mailSsl; }

    public String getMailAppBaseUrl() { return mailAppBaseUrl; }
    public void setMailAppBaseUrl(String mailAppBaseUrl) { this.mailAppBaseUrl = mailAppBaseUrl != null ? mailAppBaseUrl.trim() : ""; }
}

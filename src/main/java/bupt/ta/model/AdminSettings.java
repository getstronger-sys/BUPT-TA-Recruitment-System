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
}

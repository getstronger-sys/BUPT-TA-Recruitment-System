package bupt.ta.model;

/**
 * Admin-managed recruitment rules.
 */
public class AdminSettings {
    /** 0 means no hard limit. */
    private int maxSelectedJobsPerTa = 2;
    private boolean autoClosePendingWhenLimitReached = true;

    public int getMaxSelectedJobsPerTa() {
        return maxSelectedJobsPerTa;
    }

    public void setMaxSelectedJobsPerTa(int maxSelectedJobsPerTa) {
        this.maxSelectedJobsPerTa = Math.max(0, maxSelectedJobsPerTa);
    }

    public boolean isAutoClosePendingWhenLimitReached() {
        return autoClosePendingWhenLimitReached;
    }

    public void setAutoClosePendingWhenLimitReached(boolean autoClosePendingWhenLimitReached) {
        this.autoClosePendingWhenLimitReached = autoClosePendingWhenLimitReached;
    }

    public boolean hasWorkloadLimit() {
        return maxSelectedJobsPerTa > 0;
    }
}

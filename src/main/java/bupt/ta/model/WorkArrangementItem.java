package bupt.ta.model;

/**
 * One row of work arrangement on an MO job posting.
 * Legacy JSON may only populate {@code duration}; new postings use sessionDuration + occurrenceCount.
 */
public class WorkArrangementItem {
    private String workName;
    /** Legacy field (older saves); use sessionDuration for new data. */
    private String duration;
    private String sessionDuration;
    private int occurrenceCount;
    private int taCount;
    /** Optional; empty means TBD / arranged later. */
    private String specificTime;

    public WorkArrangementItem() {
    }

    public WorkArrangementItem(String workName, String sessionDuration, int occurrenceCount, int taCount, String specificTime) {
        this.workName = workName;
        this.sessionDuration = sessionDuration;
        this.occurrenceCount = occurrenceCount;
        this.taCount = taCount;
        this.specificTime = specificTime;
    }

    /** Effective per-session duration for display and derived text. */
    public String getResolvedSessionDuration() {
        if (sessionDuration != null && !sessionDuration.trim().isEmpty()) {
            return sessionDuration.trim();
        }
        if (duration != null && !duration.trim().isEmpty()) {
            return duration.trim();
        }
        return "";
    }

    /** Effective occurrence count (legacy rows with only {@code duration} count as 1). */
    public int getResolvedOccurrenceCount() {
        if (occurrenceCount > 0) {
            return occurrenceCount;
        }
        if (duration != null && !duration.trim().isEmpty()) {
            return 1;
        }
        return 0;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    /** @deprecated retained for Gson backward compatibility */
    public String getDuration() {
        return duration;
    }

    /** @deprecated retained for Gson backward compatibility */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public int getTaCount() {
        return taCount;
    }

    public void setTaCount(int taCount) {
        this.taCount = taCount;
    }

    public String getSpecificTime() {
        return specificTime;
    }

    public void setSpecificTime(String specificTime) {
        this.specificTime = specificTime;
    }
}

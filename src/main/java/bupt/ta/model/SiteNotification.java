package bupt.ta.model;

import com.google.gson.annotations.SerializedName;

/**
 * In-app notification (site message) for a user — persisted as JSON.
 */
public class SiteNotification {
    private String id;
    /** TA / user id who receives this message */
    private String recipientUserId;
    private String applicationId;
    private String jobId;
    /**
     * Machine-readable category for styling/filtering, e.g. APPLICATION_SUBMITTED, STATUS_INTERVIEW.
     */
    private String kind;
    private String title;
    private String body;
    /**
     * Stored in JSON as "read". Field name avoids "read" as a Java field (Gson / tooling edge cases).
     */
    @SerializedName("read")
    private boolean readFlag;
    private String createdAt;

    public SiteNotification() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(String recipientUserId) { this.recipientUserId = recipientUserId; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isRead() { return readFlag; }
    public void setRead(boolean read) { this.readFlag = read; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

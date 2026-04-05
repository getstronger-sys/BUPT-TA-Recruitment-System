package bupt.ta.model;

/**
 * Job application - TA applies for a job.
 */
public class Application {
    private String id;
    private String jobId;
    private String applicantId;  // TA user id
    private String applicantName;
    private String status;  // PENDING, INTERVIEW, WAITLIST, SELECTED, REJECTED, AUTO_CLOSED, WITHDRAWN
    private String appliedAt;
    private String notes;  // MO's notes when selecting
    /** Interview notice (in-app, not email) — set when MO sends batch/single notice */
    private String interviewTime;
    private String interviewLocation;
    private String interviewAssessment;

    public Application() {
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInterviewTime() { return interviewTime; }
    public void setInterviewTime(String interviewTime) { this.interviewTime = interviewTime; }
    public String getInterviewLocation() { return interviewLocation; }
    public void setInterviewLocation(String interviewLocation) { this.interviewLocation = interviewLocation; }
    public String getInterviewAssessment() { return interviewAssessment; }
    public void setInterviewAssessment(String interviewAssessment) { this.interviewAssessment = interviewAssessment; }
}

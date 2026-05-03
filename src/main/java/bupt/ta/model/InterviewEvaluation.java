package bupt.ta.model;

/**
 * MO interview assessment for one application.
 */
public class InterviewEvaluation {
    private String id;
    private String applicationId;
    private String jobId;
    private String applicantId;
    private String evaluatorId;
    private String evaluatorName;
    private int technicalScore;
    private int teachingScore;
    private int communicationScore;
    private int availabilityScore;
    private int responsibilityScore;
    private String recommendation; // STRONG_HIRE, HIRE, WAITLIST, REJECT
    private String strengths;
    private String concerns;
    private String internalNotes;
    private String createdAt;
    private String updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getApplicantId() { return applicantId; }
    public void setApplicantId(String applicantId) { this.applicantId = applicantId; }
    public String getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(String evaluatorId) { this.evaluatorId = evaluatorId; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    public int getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(int technicalScore) { this.technicalScore = technicalScore; }
    public int getTeachingScore() { return teachingScore; }
    public void setTeachingScore(int teachingScore) { this.teachingScore = teachingScore; }
    public int getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(int communicationScore) { this.communicationScore = communicationScore; }
    public int getAvailabilityScore() { return availabilityScore; }
    public void setAvailabilityScore(int availabilityScore) { this.availabilityScore = availabilityScore; }
    public int getResponsibilityScore() { return responsibilityScore; }
    public void setResponsibilityScore(int responsibilityScore) { this.responsibilityScore = responsibilityScore; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getConcerns() { return concerns; }
    public void setConcerns(String concerns) { this.concerns = concerns; }
    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getRawScore() {
        return clamp(technicalScore) + clamp(teachingScore) + clamp(communicationScore)
                + clamp(availabilityScore) + clamp(responsibilityScore);
    }

    public int getTotalScore() {
        return getRawScore() * 4;
    }

    public String getRecommendationLabel() {
        if ("STRONG_HIRE".equals(recommendation)) return "Strong hire";
        if ("HIRE".equals(recommendation)) return "Hire";
        if ("WAITLIST".equals(recommendation)) return "Waitlist";
        if ("REJECT".equals(recommendation)) return "Reject";
        return "Not set";
    }

    private int clamp(int value) {
        if (value < 1) return 1;
        if (value > 5) return 5;
        return value;
    }
}

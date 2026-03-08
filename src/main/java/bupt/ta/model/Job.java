package bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Job posting - created by Module Organiser.
 */
public class Job {
    private String id;
    private String title;
    private String moduleCode;  // e.g. EBU6304
    private String moduleName;
    private String description;
    private List<String> requiredSkills;
    private String postedBy;  // MO user id
    private String postedByName;
    private String status;  // OPEN, CLOSED
    private String createdAt;
    private int maxApplicants;  // 0 = unlimited
    private String jobType;  // MODULE_TA, INVIGILATION, OTHER

    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.status = "OPEN";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getMaxApplicants() { return maxApplicants; }
    public void setMaxApplicants(int maxApplicants) { this.maxApplicants = maxApplicants; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
}

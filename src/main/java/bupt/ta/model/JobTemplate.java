package bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable MO job template so common course fields do not need retyping.
 */
public class JobTemplate {
    private String id;
    private String ownerId;
    private String ownerName;
    private String templateName;
    private String title;
    private String moduleCode;
    private String moduleName;
    private String description;
    private String responsibilities;
    private String workingHours;
    private String workload;
    private String payment;
    private List<String> requiredSkills;
    private String jobType;
    private int maxApplicants;
    private boolean autoFillFromWaitlist;
    private String createdAt;

    public JobTemplate() {
        this.requiredSkills = new ArrayList<>();
        this.jobType = "MODULE_TA";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public String getWorkload() { return workload; }
    public void setWorkload(String workload) { this.workload = workload; }
    public String getPayment() { return payment; }
    public void setPayment(String payment) { this.payment = payment; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public int getMaxApplicants() { return maxApplicants; }
    public void setMaxApplicants(int maxApplicants) { this.maxApplicants = maxApplicants; }
    public boolean isAutoFillFromWaitlist() { return autoFillFromWaitlist; }
    public void setAutoFillFromWaitlist(boolean autoFillFromWaitlist) { this.autoFillFromWaitlist = autoFillFromWaitlist; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

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
    /** TA-facing: duties (separate from general description). */
    private String responsibilities;
    /** Hours per week, session times, or schedule text. */
    private String workingHours;
    /** Expected workload description (e.g. hours/week, sessions). */
    private String workload;
    /** Pay rate, stipend, or bursary text. */
    private String payment;
    /** Application deadline, ISO date yyyy-MM-dd preferred. */
    private String deadline;
    /** Suggested number of TAs for this posting. */
    private int taSlots;
    /** Course timeline such as labs, coursework and exam milestones. */
    private String examTimeline;
    /** How work should be split when multiple TAs are selected. */
    private String taAllocationPlan;
    /** Interview arrangement shown to candidates before applying. */
    private String interviewSchedule;
    /** Interview location (room/link) shown to candidates before applying. */
    private String interviewLocation;
    /** Whether selected TA withdrawals should auto-promote from waitlist. */
    private boolean autoFillFromWaitlist;
    /** Structured work items (name, duration, TA count, optional time) from MO posting form. */
    private List<WorkArrangementItem> workArrangements;

    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.workArrangements = new ArrayList<>();
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

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public String getWorkload() { return workload; }
    public void setWorkload(String workload) { this.workload = workload; }
    public String getPayment() { return payment; }
    public void setPayment(String payment) { this.payment = payment; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public int getTaSlots() { return taSlots; }
    public void setTaSlots(int taSlots) { this.taSlots = taSlots; }
    public String getExamTimeline() { return examTimeline; }
    public void setExamTimeline(String examTimeline) { this.examTimeline = examTimeline; }
    public String getTaAllocationPlan() { return taAllocationPlan; }
    public void setTaAllocationPlan(String taAllocationPlan) { this.taAllocationPlan = taAllocationPlan; }
    public String getInterviewSchedule() { return interviewSchedule; }
    public void setInterviewSchedule(String interviewSchedule) { this.interviewSchedule = interviewSchedule; }
    public String getInterviewLocation() { return interviewLocation; }
    public void setInterviewLocation(String interviewLocation) { this.interviewLocation = interviewLocation; }
    public boolean isAutoFillFromWaitlist() { return autoFillFromWaitlist; }
    public void setAutoFillFromWaitlist(boolean autoFillFromWaitlist) { this.autoFillFromWaitlist = autoFillFromWaitlist; }

    public List<WorkArrangementItem> getWorkArrangements() {
        return workArrangements != null ? workArrangements : new ArrayList<>();
    }

    public void setWorkArrangements(List<WorkArrangementItem> workArrangements) {
        this.workArrangements = workArrangements != null ? workArrangements : new ArrayList<>();
    }
}

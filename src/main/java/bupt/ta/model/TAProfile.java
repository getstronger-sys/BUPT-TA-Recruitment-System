package bupt.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * TA applicant profile - skills, availability, etc.
 */
public class TAProfile {
    private String userId;
    private String studentId;
    /** Contact email (shown to module organisers with applications). */
    private String email;
    private String phone;
    private String cvFilePath;  // relative path to uploaded CV
    private List<String> skills;  // e.g. Java, Python, Teaching
    private String availability;  // e.g. "Mon/Wed/Fri 9-12"
    private String introduction;
    /** Job ids saved by the TA for later review. */
    private List<String> savedJobIds;
    /** e.g. BSc, MSc, PhD */
    private String degree;
    /** Degree programme / major, e.g. Computer Science */
    private String programme;
    /** Current study year, e.g. Year 2 */
    private String yearOfStudy;
    /** Prior TA or teaching-related experience */
    private String taExperience;

    public TAProfile() {
        this.skills = new ArrayList<>();
        this.savedJobIds = new ArrayList<>();
    }

    public TAProfile(String userId) {
        this.userId = userId;
        this.skills = new ArrayList<>();
        this.savedJobIds = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCvFilePath() { return cvFilePath; }
    public void setCvFilePath(String cvFilePath) { this.cvFilePath = cvFilePath; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills != null ? skills : new ArrayList<>(); }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public List<String> getSavedJobIds() {
        if (savedJobIds == null) {
            savedJobIds = new ArrayList<>();
        }
        return savedJobIds;
    }
    public void setSavedJobIds(List<String> savedJobIds) { this.savedJobIds = savedJobIds != null ? savedJobIds : new ArrayList<>(); }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public String getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(String yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public String getTaExperience() { return taExperience; }
    public void setTaExperience(String taExperience) { this.taExperience = taExperience; }
}

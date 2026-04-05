package bupt.ta.service;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.SiteNotification;
import bupt.ta.storage.DataStorage;

import java.io.IOException;

/**
 * Creates in-app notifications for TA applicants when application state changes.
 * Comments describe intent for each {@link SiteNotification#getKind()} value.
 */
public class StudentNotificationService {

    public static final String KIND_APPLICATION_SUBMITTED = "APPLICATION_SUBMITTED";
    public static final String KIND_STATUS_INTERVIEW = "STATUS_INTERVIEW";
    public static final String KIND_STATUS_WAITLIST = "STATUS_WAITLIST";
    public static final String KIND_STATUS_SELECTED = "STATUS_SELECTED";
    public static final String KIND_STATUS_REJECTED = "STATUS_REJECTED";
    public static final String KIND_INTERVIEW_DETAILS = "INTERVIEW_DETAILS";
    public static final String KIND_AUTO_CLOSED = "AUTO_CLOSED";
    public static final String KIND_WITHDRAWN = "WITHDRAWN";
    public static final String KIND_AUTO_PROMOTED = "AUTO_PROMOTED";

    private static String jobLabel(Job job, String jobId) {
        if (job != null && job.getTitle() != null && !job.getTitle().trim().isEmpty()) {
            String code = job.getModuleCode() != null && !job.getModuleCode().trim().isEmpty()
                    ? " (" + job.getModuleCode().trim() + ")" : "";
            return job.getTitle().trim() + code;
        }
        return jobId != null ? jobId : "Unknown job";
    }

    public static void notifyApplicationSubmitted(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_APPLICATION_SUBMITTED);
        n.setTitle("Application received");
        n.setBody("Your application for " + jobLabel(job, app.getJobId()) + " was submitted successfully. "
                + "You will receive in-app updates when the module organiser reviews your application.");
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyInterviewInvite(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_STATUS_INTERVIEW);
        n.setTitle("Interview stage");
        n.setBody("Your application for " + jobLabel(job, app.getJobId())
                + " has moved to the interview stage. Check My Applications for details; "
                + "the organiser may post time and location in-app.");
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyWaitlist(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_STATUS_WAITLIST);
        n.setTitle("Waitlist");
        n.setBody("Your application for " + jobLabel(job, app.getJobId())
                + " is now on the waitlist. You will be notified if the status changes.");
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifySelected(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_STATUS_SELECTED);
        n.setTitle("Application successful");
        n.setBody("Congratulations — you were selected for " + jobLabel(job, app.getJobId()) + ".");
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyRejected(DataStorage storage, Application app, Job job, String moNotes) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_STATUS_REJECTED);
        n.setTitle("Application update");
        StringBuilder body = new StringBuilder();
        body.append("Your application for ").append(jobLabel(job, app.getJobId()))
                .append(" was not successful.");
        if (moNotes != null && !moNotes.trim().isEmpty()) {
            body.append(" Message from organiser: ").append(moNotes.trim());
        }
        n.setBody(body.toString());
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyInterviewDetails(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        String time = app.getInterviewTime() != null ? app.getInterviewTime().trim() : "";
        String loc = app.getInterviewLocation() != null ? app.getInterviewLocation().trim() : "";
        String assess = app.getInterviewAssessment() != null ? app.getInterviewAssessment().trim() : "";
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_INTERVIEW_DETAILS);
        n.setTitle("Interview information");
        StringBuilder body = new StringBuilder();
        body.append("Interview details for ").append(jobLabel(job, app.getJobId())).append(":\n");
        body.append("Time: ").append(time.isEmpty() ? "—" : time).append("\n");
        body.append("Location: ").append(loc.isEmpty() ? "—" : loc);
        if (!assess.isEmpty()) {
            body.append("\nAssessment / notes: ").append(assess);
        }
        n.setBody(body.toString());
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyAutoClosed(DataStorage storage, Application app, Job job, String note) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_AUTO_CLOSED);
        n.setTitle("Application closed automatically");
        String extra = note != null && !note.trim().isEmpty() ? " " + note.trim() : "";
        n.setBody("Your pending application for " + jobLabel(job, app.getJobId())
                + " was closed automatically because you reached the maximum number of selected positions." + extra);
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyWithdrawn(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_WITHDRAWN);
        n.setTitle("Application withdrawn");
        n.setBody("You withdrew your application for " + jobLabel(job, app.getJobId()) + ".");
        n.setRead(false);
        storage.addSiteNotification(n);
    }

    public static void notifyAutoPromotedFromWaitlist(DataStorage storage, Application app, Job job) throws IOException {
        if (app == null || app.getApplicantId() == null) {
            return;
        }
        SiteNotification n = new SiteNotification();
        n.setRecipientUserId(app.getApplicantId());
        n.setApplicationId(app.getId());
        n.setJobId(app.getJobId());
        n.setKind(KIND_AUTO_PROMOTED);
        n.setTitle("Promoted from waitlist");
        n.setBody("You were automatically selected for " + jobLabel(job, app.getJobId())
                + " from the waitlist after a vacancy opened.");
        n.setRead(false);
        storage.addSiteNotification(n);
    }
}

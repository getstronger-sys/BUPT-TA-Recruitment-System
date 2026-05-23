package bupt.ta.service;

import bupt.ta.model.Application;
import bupt.ta.model.ApplicationEvent;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper for writing application audit timeline events consistently.
 * Event type constants match values stored in {@link ApplicationEvent#getEventType()}.
 */
public class ApplicationTimelineService {
    /** Application submitted by TA. */
    public static final String TYPE_SUBMITTED = "SUBMITTED";
    /** Status transition recorded on an application. */
    public static final String TYPE_STATUS_CHANGED = "STATUS_CHANGED";
    /** Interview notice sent to applicant. */
    public static final String TYPE_INTERVIEW_NOTICE = "INTERVIEW_NOTICE";
    /** TA booked an interview slot. */
    public static final String TYPE_INTERVIEW_BOOKED = "INTERVIEW_BOOKED";
    /** TA cancelled a booked interview slot. */
    public static final String TYPE_INTERVIEW_CANCELLED = "INTERVIEW_CANCELLED";
    /** MO saved interview evaluation. */
    public static final String TYPE_EVALUATION_SAVED = "EVALUATION_SAVED";
    /** Final select/reject decision recorded. */
    public static final String TYPE_DECISION_RECORDED = "DECISION_RECORDED";
    /** TA withdrew the application. */
    public static final String TYPE_WITHDRAWN = "WITHDRAWN";
    /** Waitlisted applicant auto-promoted to selected. */
    public static final String TYPE_AUTO_PROMOTED = "AUTO_PROMOTED";

    /** Whether the applicant may see this event on the TA timeline (MO internal scoring is hidden). */
    public static boolean isVisibleToApplicant(ApplicationEvent event) {
        return event != null && !TYPE_EVALUATION_SAVED.equals(event.getEventType());
    }

    /** Returns timeline events safe to show on the TA applications page. */
    public static List<ApplicationEvent> filterForApplicantView(List<ApplicationEvent> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        return events.stream()
                .filter(ApplicationTimelineService::isVisibleToApplicant)
                .collect(Collectors.toList());
    }

    /** Filters grouped timeline events for the TA applications page. */
    public static Map<String, List<ApplicationEvent>> filterForApplicantView(
            Map<String, List<ApplicationEvent>> byApplicationId) {
        if (byApplicationId == null || byApplicationId.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<ApplicationEvent>> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, List<ApplicationEvent>> entry : byApplicationId.entrySet()) {
            filtered.put(entry.getKey(), filterForApplicantView(entry.getValue()));
        }
        return filtered;
    }

    /**
     * Appends one timeline event for an application.
     *
     * @param storage     persistence
     * @param app         application record
     * @param job         related job (unused, reserved for callers)
     * @param actorUserId acting user id
     * @param actorName   acting user display name
     * @param actorRole   TA, MO, ADMIN, or SYSTEM
     * @param eventType   {@link #TYPE_SUBMITTED} and related constants
     * @param title       short event title
     * @param detail      optional longer detail
     * @param fromStatus  previous status (may be empty)
     * @param toStatus    new status (may be empty)
     * @throws IOException if persistence fails
     */
    public void record(DataStorage storage, Application app, Job job,
                       String actorUserId, String actorName, String actorRole,
                       String eventType, String title, String detail,
                       String fromStatus, String toStatus) throws IOException {
        if (storage == null || app == null || app.getId() == null) {
            return;
        }
        ApplicationEvent event = new ApplicationEvent();
        event.setApplicationId(app.getId());
        event.setJobId(app.getJobId());
        event.setApplicantId(app.getApplicantId());
        event.setActorUserId(clean(actorUserId));
        event.setActorName(clean(actorName));
        event.setActorRole(clean(actorRole));
        event.setEventType(clean(eventType));
        event.setTitle(clean(title));
        event.setDetail(clean(detail));
        event.setFromStatus(clean(fromStatus));
        event.setToStatus(clean(toStatus));
        storage.addApplicationEvent(event);
    }

    /**
     * Records a {@link #TYPE_STATUS_CHANGED} timeline event.
     *
     * @throws IOException if persistence fails
     */
    public void recordStatusChange(DataStorage storage, Application app, Job job,
                                   String actorUserId, String actorName, String actorRole,
                                   String fromStatus, String toStatus, String detail) throws IOException {
        record(storage, app, job, actorUserId, actorName, actorRole,
                TYPE_STATUS_CHANGED, "Status changed", detail, fromStatus, toStatus);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

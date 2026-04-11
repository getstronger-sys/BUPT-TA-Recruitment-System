package bupt.ta.util;

import bupt.ta.model.Application;
import bupt.ta.model.Job;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Builds simple iCalendar payloads for interview appointments.
 */
public final class InterviewCalendarSupport {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );
    private static final DateTimeFormatter ICS_LOCAL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final int DEFAULT_DURATION_MINUTES = 45;

    private InterviewCalendarSupport() {
    }

    public static LocalDateTime parseInterviewTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim();
        for (DateTimeFormatter formatter : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted format.
            }
        }
        return null;
    }

    public static String buildCalendarFile(Application application, Job job) {
        LocalDateTime start = parseInterviewTime(application != null ? application.getInterviewTime() : null);
        if (application == null || start == null) {
            throw new IllegalArgumentException("Interview time must use yyyy-MM-dd HH:mm or yyyy-MM-dd'T'HH:mm.");
        }

        LocalDateTime end = start.plusMinutes(DEFAULT_DURATION_MINUTES);
        String jobTitle = job != null && job.getTitle() != null && !job.getTitle().trim().isEmpty()
                ? job.getTitle().trim()
                : "TA interview";
        String moduleCode = job != null && job.getModuleCode() != null ? job.getModuleCode().trim() : "";
        String summary = moduleCode.isEmpty()
                ? "Interview - " + jobTitle
                : "Interview - " + jobTitle + " (" + moduleCode + ")";

        StringBuilder description = new StringBuilder();
        description.append("Teaching Assistant recruitment interview");
        if (!moduleCode.isEmpty()) {
            description.append("\\nModule: ").append(escape(moduleCode));
        }
        if (job != null && job.getModuleName() != null && !job.getModuleName().trim().isEmpty()) {
            description.append("\\nModule name: ").append(escape(job.getModuleName().trim()));
        }
        if (application.getPreferredRole() != null && !application.getPreferredRole().trim().isEmpty()) {
            description.append("\\nPreferred role: ").append(escape(application.getPreferredRole().trim()));
        }
        if (application.getInterviewAssessment() != null && !application.getInterviewAssessment().trim().isEmpty()) {
            description.append("\\nAssessment: ").append(escape(application.getInterviewAssessment().trim()));
        }
        description.append("\\nApplication ID: ").append(escape(application.getId() != null ? application.getId() : "unknown"));

        String location = application.getInterviewLocation() != null ? application.getInterviewLocation().trim() : "";
        String uid = (application.getId() != null ? application.getId() : "interview")
                + "@bupt-ta-recruitment";

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//BUPT TA Recruitment//Interview Calendar//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(escape(uid)).append("\r\n");
        ics.append("DTSTAMP:").append(LocalDateTime.now(ZoneOffset.UTC).format(ICS_UTC)).append("\r\n");
        ics.append("DTSTART:").append(start.format(ICS_LOCAL)).append("\r\n");
        ics.append("DTEND:").append(end.format(ICS_LOCAL)).append("\r\n");
        ics.append("SUMMARY:").append(escape(summary)).append("\r\n");
        if (!location.isEmpty()) {
            ics.append("LOCATION:").append(escape(location)).append("\r\n");
        }
        ics.append("DESCRIPTION:").append(description).append("\r\n");
        ics.append("STATUS:CONFIRMED\r\n");
        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");
        return ics.toString();
    }

    public static String buildFilename(Application application, Job job) {
        String base = job != null && job.getModuleCode() != null && !job.getModuleCode().trim().isEmpty()
                ? job.getModuleCode().trim()
                : (job != null && job.getTitle() != null ? job.getTitle().trim() : "interview");
        String normalized = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (normalized.isEmpty()) {
            normalized = "interview";
        }
        String appId = application != null && application.getId() != null ? application.getId().trim() : "event";
        return normalized + "-" + appId + ".ics";
    }

    private static String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }
}

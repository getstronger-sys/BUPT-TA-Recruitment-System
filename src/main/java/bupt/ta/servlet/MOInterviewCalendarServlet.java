package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.InterviewSlot;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import bupt.ta.util.JobActivity;

/**
 * MO view: aggregate interview-stage applications across own postings into a simple date-grouped calendar.
 */
public class MOInterviewCalendarServlet extends HttpServlet {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FMT_YMD_HM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);
    private static final DateTimeFormatter FMT_SLASH_YMD_HM = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.ROOT);
    private static final DateTimeFormatter FMT_SLASH_YMD = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);
    private static final Pattern LEADING_ISO_DATE = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
    /** yyyy-M-d or yyyy/M/d at start of string (single-digit month/day allowed). */
    private static final Pattern FLEX_LEADING_DATE = Pattern.compile("^\\s*(\\d{4})[\\-/](\\d{1,2})[\\-/](\\d{1,2})(?![0-9])");

    public static final class CalendarRow {
        private final LocalDate date;
        private final String timeDisplay;
        private final String applicantName;
        private final String jobTitle;
        private final String moduleCode;
        private final String location;
        private final String jobId;
        private final String applicationId;
        private final String status;
        private final String listPath;
        private final String interviewTimeRaw;

        public CalendarRow(LocalDate date, String timeDisplay, String applicantName, String jobTitle,
                           String moduleCode, String location, String jobId, String applicationId, String status,
                           String listPath, String interviewTimeRaw) {
            this.date = date;
            this.timeDisplay = timeDisplay;
            this.applicantName = applicantName;
            this.jobTitle = jobTitle;
            this.moduleCode = moduleCode;
            this.location = location;
            this.jobId = jobId;
            this.applicationId = applicationId;
            this.status = status;
            this.listPath = listPath;
            this.interviewTimeRaw = interviewTimeRaw;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getTimeDisplay() {
            return timeDisplay;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getModuleCode() {
            return moduleCode;
        }

        public String getLocation() {
            return location;
        }

        public String getJobId() {
            return jobId;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getStatus() {
            return status;
        }

        public String getInterviewTimeRaw() {
            return interviewTimeRaw;
        }

        public String manageHref(String contextPath) {
            return contextPath + listPath + "?jobId=" + URLEncoder.encode(jobId, StandardCharsets.UTF_8) + "&view=interview";
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        storage.syncJobStatusesWithDeadlines();

        List<Job> myJobs = storage.loadJobs().stream()
                .filter(j -> moId.equals(j.getPostedBy()))
                .collect(Collectors.toList());
        Map<String, Job> jobById = new HashMap<>();
        for (Job j : myJobs) {
            jobById.put(j.getId(), j);
        }

        List<Application> apps = storage.loadApplications();
        Map<String, InterviewSlot> slotById = new HashMap<>();
        for (InterviewSlot slot : storage.loadInterviewSlots()) {
            slotById.put(slot.getId(), slot);
        }
        List<CalendarRow> scheduled = new ArrayList<>();
        List<CalendarRow> unscheduled = new ArrayList<>();

        for (Application a : apps) {
            if (!"INTERVIEW".equals(a.getStatus()) && !"WAITLIST".equals(a.getStatus())) {
                continue;
            }
            Job job = jobById.get(a.getJobId());
            if (job == null) {
                continue;
            }
            InterviewSlot slot = a.getInterviewSlotId() != null ? slotById.get(a.getInterviewSlotId()) : null;
            String timeRaw = slot != null && slot.getStartsAt() != null ? slot.getStartsAt().trim()
                    : (a.getInterviewTime() != null ? a.getInterviewTime().trim() : "");
            String loc = slot != null && slot.getLocation() != null ? slot.getLocation().trim()
                    : (a.getInterviewLocation() != null ? a.getInterviewLocation().trim() : "");
            String applicant = a.getApplicantName() != null && !a.getApplicantName().isEmpty()
                    ? a.getApplicantName() : a.getApplicantId();
            String title = job.getTitle() != null ? job.getTitle() : a.getJobId();
            String module = job.getModuleCode() != null ? job.getModuleCode() : "";

            LocalDate parsed = parseInterviewDate(timeRaw);
            String timeDisplay = formatTimeDisplay(timeRaw, parsed);
            String listPath = JobActivity.listPathFor(job);

            CalendarRow row = new CalendarRow(
                    parsed,
                    timeDisplay,
                    applicant,
                    title,
                    module,
                    loc,
                    job.getId(),
                    a.getId(),
                    a.getStatus(),
                    listPath,
                    timeRaw
            );
            if (parsed == null) {
                unscheduled.add(row);
            } else {
                scheduled.add(row);
            }
        }

        LocalDate today = LocalDate.now();
        List<CalendarRow> expiredRows = new ArrayList<>();
        List<CalendarRow> upcomingRows = new ArrayList<>();
        for (CalendarRow r : scheduled) {
            if (r.getDate() != null && r.getDate().isBefore(today)) {
                expiredRows.add(r);
            } else {
                upcomingRows.add(r);
            }
        }
        expiredRows.sort(Comparator
                .comparing(CalendarRow::getDate).reversed()
                .thenComparing(CalendarRow::getTimeDisplay, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CalendarRow::getApplicantName, String.CASE_INSENSITIVE_ORDER));

        upcomingRows.sort(Comparator
                .comparing(CalendarRow::getDate)
                .thenComparing(CalendarRow::getTimeDisplay, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CalendarRow::getApplicantName, String.CASE_INSENSITIVE_ORDER));

        TreeMap<LocalDate, List<CalendarRow>> byDay = new TreeMap<>();
        for (CalendarRow r : upcomingRows) {
            byDay.computeIfAbsent(r.getDate(), d -> new ArrayList<>()).add(r);
        }
        for (List<CalendarRow> dayRows : byDay.values()) {
            dayRows.sort(Comparator
                    .comparing(CalendarRow::getTimeDisplay, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(CalendarRow::getApplicantName, String.CASE_INSENSITIVE_ORDER));
        }

        unscheduled.sort(Comparator.comparing(CalendarRow::getApplicantName, String.CASE_INSENSITIVE_ORDER));

        req.setAttribute("moNavActive", "calendar");
        req.setAttribute("calendarByDay", byDay);
        req.setAttribute("calendarExpiredRows", expiredRows);
        req.setAttribute("calendarUnscheduled", unscheduled);
        req.setAttribute("calendarTotalUpcoming", upcomingRows.size());
        req.setAttribute("calendarTotalExpired", expiredRows.size());
        req.setAttribute("calendarTotalUnscheduled", unscheduled.size());
        req.getRequestDispatcher("/mo/interview-calendar.jsp").forward(req, resp);
    }

    /** Best-effort parse of free-text interview time; returns null if unknown. */
    static LocalDate parseInterviewDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String t = raw.trim();

        Matcher flex = FLEX_LEADING_DATE.matcher(t);
        if (flex.find()) {
            try {
                int y = Integer.parseInt(flex.group(1), 10);
                int mo = Integer.parseInt(flex.group(2), 10);
                int d = Integer.parseInt(flex.group(3), 10);
                return LocalDate.of(y, mo, d);
            } catch (Exception ignored) {
                // fall through
            }
        }

        Matcher m = LEADING_ISO_DATE.matcher(t);
        if (m.find()) {
            try {
                return LocalDate.parse(m.group(0), ISO_DATE);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }

        String normalized = t.replace('T', ' ');
        int dot = normalized.indexOf('.');
        if (dot > 0) {
            normalized = normalized.substring(0, dot);
        }
        int z = normalized.toUpperCase(Locale.ROOT).indexOf('Z');
        if (z > 0) {
            normalized = normalized.substring(0, z).trim();
        }
        int plus = normalized.indexOf('+', 10);
        if (plus > 0) {
            normalized = normalized.substring(0, plus).trim();
        }

        try {
            return LocalDateTime.parse(normalized, FMT_YMD_HM).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalDateTime.parse(normalized, FMT_SLASH_YMD_HM).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalDate.parse(normalized, FMT_SLASH_YMD);
        } catch (DateTimeParseException ignored) {
            // continue
        }

        try {
            return LocalDateTime.parse(normalized.replace(' ', 'T')).toLocalDate();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String formatTimeDisplay(String raw, LocalDate date) {
        if (raw == null || raw.trim().isEmpty()) {
            return "—";
        }
        String t = raw.trim();
        if (date != null && t.length() > 10) {
            String rest = t.substring(10).trim();
            if (rest.startsWith("T")) {
                rest = rest.substring(1).trim();
            }
            if (!rest.isEmpty()) {
                return rest;
            }
        }
        return t.length() > 10 ? t.substring(10).trim() : "All day";
    }
}

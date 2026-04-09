package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.model.WorkArrangementItem;
import bupt.ta.storage.DataStorage;
import bupt.ta.util.WorkArrangementSupport;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostJobServlet extends HttpServlet {

    private static final int MIN_RESPONSIBILITIES_LEN = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = trim(req.getParameter("title"));
        String moduleCode = trim(req.getParameter("moduleCode"));
        String moduleName = trim(req.getParameter("moduleName"));
        String description = trim(req.getParameter("description"));
        String responsibilities = trim(req.getParameter("responsibilities"));
        String payment = trim(req.getParameter("payment"));
        String deadline = trim(req.getParameter("deadline"));
        String examTimeline = trim(req.getParameter("examTimeline"));
        String interviewSchedule = trim(req.getParameter("interviewSchedule"));
        String interviewLocation = trim(req.getParameter("interviewLocation"));
        String skillsStr = req.getParameter("skills");
        String maxApplicantsStr = req.getParameter("maxApplicants");
        String plannedTaCountStr = req.getParameter("plannedTaCount");
        String jobType = req.getParameter("jobType");
        String postedBy = (String) req.getSession().getAttribute("userId");
        String postedByName = (String) req.getSession().getAttribute("realName");

        if (postedByName == null) {
            postedByName = (String) req.getSession().getAttribute("username");
        }

        List<String> skills = skillsStr != null && !skillsStr.trim().isEmpty()
                ? Arrays.stream(skillsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                : Arrays.asList();

        int maxApplicants = 0;
        try {
            if (maxApplicantsStr != null && !maxApplicantsStr.trim().isEmpty()) {
                maxApplicants = Integer.parseInt(maxApplicantsStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        int plannedTaCount = 0;
        try {
            if (plannedTaCountStr != null && !plannedTaCountStr.trim().isEmpty()) {
                plannedTaCount = Integer.parseInt(plannedTaCountStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        List<WorkArrangementItem> workRows = parseWorkArrangements(req);
        String error = validateWorkArrangements(workRows);
        if (error == null) {
            error = validateJobForm(title, moduleCode, moduleName, responsibilities, payment, deadline, examTimeline,
                    interviewSchedule, interviewLocation, skills, maxApplicants, plannedTaCount);
        }

        if (error != null) {
            repopulateForm(req, title, moduleCode, moduleName, description, responsibilities, payment, deadline,
                    examTimeline, interviewSchedule, interviewLocation, skillsStr, maxApplicantsStr, jobType, workRows,
                    req.getParameter("autoFillFromWaitlist") != null, plannedTaCountStr);
            req.setAttribute("error", error);
            req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
            return;
        }

        Job job = new Job();
        job.setTitle(title);
        job.setModuleCode(moduleCode.toUpperCase());
        job.setModuleName(moduleName);
        job.setDescription(description);
        job.setResponsibilities(responsibilities);
        WorkArrangementSupport.applyDerivedFields(job, workRows);
        job.setTaSlots(plannedTaCount);
        job.setPayment(payment);
        job.setDeadline(deadline);
        job.setExamTimeline(examTimeline);
        job.setInterviewSchedule(interviewSchedule);
        job.setInterviewLocation(interviewLocation);
        job.setRequiredSkills(skills);
        job.setPostedBy(postedBy);
        job.setPostedByName(postedByName != null ? postedByName : "MO");
        job.setMaxApplicants(maxApplicants);
        job.setJobType(jobType != null && !jobType.isEmpty() ? jobType : "MODULE_TA");
        job.setAutoFillFromWaitlist(req.getParameter("autoFillFromWaitlist") != null);

        DataStorage storage = new DataStorage(getServletContext());
        storage.addJob(job);
        resp.sendRedirect(req.getContextPath() + "/mo/job?posted=1&jobId="
                + java.net.URLEncoder.encode(job.getId(), java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    private static List<WorkArrangementItem> parseWorkArrangements(HttpServletRequest req) {
        String[] names = req.getParameterValues("waWorkName");
        if (names == null || names.length == 0) {
            return new ArrayList<>();
        }
        String[] sessionDurs = req.getParameterValues("waSessionDuration");
        String[] occCounts = req.getParameterValues("waOccurrenceCount");
        String[] taCounts = req.getParameterValues("waTaCount");
        String[] times = req.getParameterValues("waSpecificTime");
        List<WorkArrangementItem> out = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String n = trim(names[i]);
            String sd = sessionDurs != null && i < sessionDurs.length ? trim(sessionDurs[i]) : "";
            int oc = 0;
            if (occCounts != null && i < occCounts.length) {
                try {
                    oc = Integer.parseInt(trim(occCounts[i]));
                } catch (NumberFormatException ignored) {
                }
            }
            int tc = 0;
            if (taCounts != null && i < taCounts.length) {
                try {
                    tc = Integer.parseInt(trim(taCounts[i]));
                } catch (NumberFormatException ignored) {
                }
            }
            String t = times != null && i < times.length ? trim(times[i]) : "";
            if (n.isEmpty() && sd.isEmpty() && oc <= 0 && tc <= 0 && t.isEmpty()) {
                continue;
            }
            WorkArrangementItem it = new WorkArrangementItem();
            it.setWorkName(n);
            it.setSessionDuration(sd);
            it.setOccurrenceCount(oc);
            it.setTaCount(tc);
            it.setSpecificTime(t);
            out.add(it);
        }
        return out;
    }

    private static String validateWorkArrangements(List<WorkArrangementItem> items) {
        if (items.isEmpty()) {
            return "Add at least one work arrangement row (work name, per-session duration, occurrences, and TA count are required).";
        }
        for (int i = 0; i < items.size(); i++) {
            WorkArrangementItem it = items.get(i);
            if (trim(it.getWorkName()).isEmpty()) {
                return "Work arrangement row " + (i + 1) + ": work name is required.";
            }
            if (trim(it.getResolvedSessionDuration()).isEmpty()) {
                return "Work arrangement row " + (i + 1) + ": per-session duration is required.";
            }
            if (it.getOccurrenceCount() < 1) {
                return "Work arrangement row " + (i + 1) + ": number of occurrences must be at least 1.";
            }
            if (it.getTaCount() < 1) {
                return "Work arrangement row " + (i + 1) + ": TA count must be at least 1.";
            }
        }
        return null;
    }

    private static String validateJobForm(String title, String moduleCode, String moduleName,
                                          String responsibilities, String payment, String deadline, String examTimeline,
                                          String interviewSchedule, String interviewLocation,
                                          List<String> skills, int maxApplicants, int plannedTaCount) {
        if (title.isEmpty()) {
            return "Job title is required.";
        }
        if (moduleCode.isEmpty()) {
            return "Module code is required.";
        }
        if (moduleName.isEmpty()) {
            return "Module name is required.";
        }
        if (responsibilities.length() < MIN_RESPONSIBILITIES_LEN) {
            return "Responsibilities must be at least " + MIN_RESPONSIBILITIES_LEN + " characters.";
        }
        if (payment.isEmpty()) {
            return "Payment / compensation is required.";
        }
        if (deadline.isEmpty()) {
            return "Application deadline is required.";
        }
        try {
            LocalDate d = LocalDate.parse(deadline);
            if (d.isBefore(LocalDate.now())) {
                return "Application deadline must be today or a future date.";
            }
        } catch (DateTimeParseException e) {
            return "Deadline must be a valid date (YYYY-MM-DD).";
        }
        if (skills.isEmpty()) {
            return "At least one required skill is needed (comma-separated).";
        }
        if (maxApplicants < 0) {
            return "Max applicants cannot be negative.";
        }
        if (plannedTaCount < 1) {
            return "Planned recruits must be at least 1.";
        }
        if (examTimeline.isEmpty()) {
            return "Course timeline / exam milestones are required.";
        }
        if (interviewSchedule.isEmpty()) {
            return "Interview schedule is required.";
        }
        if (interviewLocation.isEmpty()) {
            return "Interview location is required.";
        }
        return null;
    }

    private static void repopulateForm(HttpServletRequest req, String title, String moduleCode, String moduleName,
                                       String description, String responsibilities, String payment, String deadline,
                                       String examTimeline, String interviewSchedule, String interviewLocation,
                                       String skillsStr, String maxApplicantsStr, String jobType,
                                       List<WorkArrangementItem> workRows, boolean autoFillFromWaitlist,
                                       String plannedTaCountStr) {
        req.setAttribute("fvTitle", title);
        req.setAttribute("fvModuleCode", moduleCode);
        req.setAttribute("fvModuleName", moduleName);
        req.setAttribute("fvDescription", description);
        req.setAttribute("fvResponsibilities", responsibilities);
        req.setAttribute("fvPayment", payment);
        req.setAttribute("fvDeadline", deadline);
        req.setAttribute("fvExamTimeline", examTimeline);
        req.setAttribute("fvInterviewSchedule", interviewSchedule);
        req.setAttribute("fvInterviewLocation", interviewLocation);
        req.setAttribute("fvSkills", skillsStr != null ? skillsStr : "");
        req.setAttribute("fvMaxApplicants", maxApplicantsStr != null ? maxApplicantsStr : "0");
        req.setAttribute("fvJobType", jobType != null ? jobType : "MODULE_TA");
        req.setAttribute("fvWorkArrangements", workRows != null ? workRows : new ArrayList<>());
        req.setAttribute("fvAutoFillFromWaitlist", autoFillFromWaitlist);
        req.setAttribute("fvPlannedTaCount", plannedTaCountStr != null ? plannedTaCountStr : "");
    }
}

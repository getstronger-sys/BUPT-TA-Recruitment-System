package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.model.JobTemplate;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostJobServlet extends HttpServlet {

    private static final int MIN_RESPONSIBILITIES_LEN = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        req.setAttribute("jobTemplates", storage.getJobTemplatesByOwner(moId));
        String templateId = trim(req.getParameter("templateId"));
        if (!templateId.isEmpty()) {
            JobTemplate template = storage.getJobTemplateById(templateId);
            if (template != null && moId.equals(template.getOwnerId())) {
                repopulateForm(req,
                        template.getTitle(),
                        template.getModuleCode(),
                        template.getModuleName(),
                        template.getDescription(),
                        template.getResponsibilities(),
                        template.getWorkingHours(),
                        template.getWorkload(),
                        template.getPayment(),
                        "",
                        String.join(", ", template.getRequiredSkills()),
                        String.valueOf(template.getMaxApplicants()),
                        template.getJobType(),
                        template.isAutoFillFromWaitlist(),
                        template.getTemplateName());
                req.setAttribute("selectedTemplateId", template.getId());
            }
        }
        req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = trim(req.getParameter("title"));
        String moduleCode = trim(req.getParameter("moduleCode"));
        String moduleName = trim(req.getParameter("moduleName"));
        String description = trim(req.getParameter("description"));
        String responsibilities = trim(req.getParameter("responsibilities"));
        String workingHours = trim(req.getParameter("workingHours"));
        String workload = trim(req.getParameter("workload"));
        String payment = trim(req.getParameter("payment"));
        String deadline = trim(req.getParameter("deadline"));
        String skillsStr = req.getParameter("skills");
        String maxApplicantsStr = req.getParameter("maxApplicants");
        String jobType = req.getParameter("jobType");
        boolean autoFillFromWaitlist = req.getParameter("autoFillFromWaitlist") != null;
        boolean saveAsTemplate = req.getParameter("saveAsTemplate") != null;
        String templateName = trim(req.getParameter("templateName"));
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

        String error = validateJobForm(title, moduleCode, moduleName, responsibilities, workingHours, workload, payment, deadline, skills, maxApplicants);

        if (error != null) {
            repopulateForm(req, title, moduleCode, moduleName, description, responsibilities, workingHours, workload, payment, deadline, skillsStr, maxApplicantsStr, jobType, autoFillFromWaitlist, templateName);
            req.setAttribute("error", error);
            req.setAttribute("jobTemplates", new DataStorage(getServletContext()).getJobTemplatesByOwner(postedBy));
            req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
            return;
        }

        Job job = new Job();
        job.setTitle(title);
        job.setModuleCode(moduleCode.toUpperCase());
        job.setModuleName(moduleName);
        job.setDescription(description);
        job.setResponsibilities(responsibilities);
        job.setWorkingHours(workingHours);
        job.setWorkload(workload);
        job.setPayment(payment);
        job.setDeadline(deadline);
        job.setRequiredSkills(skills);
        job.setPostedBy(postedBy);
        job.setPostedByName(postedByName != null ? postedByName : "MO");
        job.setMaxApplicants(maxApplicants);
        job.setJobType(jobType != null && !jobType.isEmpty() ? jobType : "MODULE_TA");
        job.setAutoFillFromWaitlist(autoFillFromWaitlist);

        DataStorage storage = new DataStorage(getServletContext());
        storage.addJob(job);
        if (saveAsTemplate) {
            JobTemplate template = new JobTemplate();
            template.setOwnerId(postedBy);
            template.setOwnerName(postedByName != null ? postedByName : "MO");
            template.setTemplateName(!templateName.isEmpty() ? templateName : defaultTemplateName(moduleCode, title));
            template.setTitle(title);
            template.setModuleCode(moduleCode.toUpperCase());
            template.setModuleName(moduleName);
            template.setDescription(description);
            template.setResponsibilities(responsibilities);
            template.setWorkingHours(workingHours);
            template.setWorkload(workload);
            template.setPayment(payment);
            template.setRequiredSkills(skills);
            template.setJobType(jobType != null && !jobType.isEmpty() ? jobType : "MODULE_TA");
            template.setMaxApplicants(maxApplicants);
            template.setAutoFillFromWaitlist(autoFillFromWaitlist);
            storage.addJobTemplate(template);
        }
        resp.sendRedirect(req.getContextPath() + "/mo/jobs?success=1&jobId="
                + java.net.URLEncoder.encode(job.getId(), java.nio.charset.StandardCharsets.UTF_8) + "&view=pending");
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    /**
     * @return error message or null if valid
     */
    private static String validateJobForm(String title, String moduleCode, String moduleName,
                                           String responsibilities, String workingHours, String workload,
                                           String payment, String deadline, List<String> skills, int maxApplicants) {
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
        if (workingHours.isEmpty()) {
            return "Working hours / schedule is required.";
        }
        if (workload.isEmpty()) {
            return "Workload is required.";
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
        return null;
    }

    private static String defaultTemplateName(String moduleCode, String title) {
        if (moduleCode != null && !moduleCode.isEmpty()) {
            return moduleCode.toUpperCase() + " template";
        }
        return title != null && !title.isEmpty() ? title + " template" : "Reusable template";
    }

    private static void repopulateForm(HttpServletRequest req, String title, String moduleCode, String moduleName,
                                       String description, String responsibilities, String workingHours,
                                       String workload, String payment, String deadline, String skillsStr,
                                       String maxApplicantsStr, String jobType, boolean autoFillFromWaitlist,
                                       String templateName) {
        req.setAttribute("fvTitle", title);
        req.setAttribute("fvModuleCode", moduleCode);
        req.setAttribute("fvModuleName", moduleName);
        req.setAttribute("fvDescription", description);
        req.setAttribute("fvResponsibilities", responsibilities);
        req.setAttribute("fvWorkingHours", workingHours);
        req.setAttribute("fvWorkload", workload);
        req.setAttribute("fvPayment", payment);
        req.setAttribute("fvDeadline", deadline);
        req.setAttribute("fvSkills", skillsStr != null ? skillsStr : "");
        req.setAttribute("fvMaxApplicants", maxApplicantsStr != null ? maxApplicantsStr : "0");
        req.setAttribute("fvJobType", jobType != null ? jobType : "MODULE_TA");
        req.setAttribute("fvAutoFillFromWaitlist", autoFillFromWaitlist);
        req.setAttribute("fvTemplateName", templateName != null ? templateName : "");
    }
}

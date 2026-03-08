package bupt.ta.servlet;

import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PostJobServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = req.getParameter("title");
        String moduleCode = req.getParameter("moduleCode");
        String moduleName = req.getParameter("moduleName");
        String description = req.getParameter("description");
        String skillsStr = req.getParameter("skills");
        String maxApplicantsStr = req.getParameter("maxApplicants");
        String jobType = req.getParameter("jobType");
        String postedBy = (String) req.getSession().getAttribute("userId");
        String postedByName = (String) req.getSession().getAttribute("realName");

        if (postedByName == null) {
            postedByName = (String) req.getSession().getAttribute("username");
        }

        String error = null;
        if (title == null || title.trim().isEmpty()) {
            error = "Job title is required.";
        } else if (moduleCode == null || moduleCode.trim().isEmpty()) {
            error = "Module code is required.";
        } else {
            List<String> skills = skillsStr != null && !skillsStr.trim().isEmpty()
                    ? Arrays.stream(skillsStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())
                    : Arrays.asList();

            int maxApplicants = 0;
            try {
                if (maxApplicantsStr != null && !maxApplicantsStr.trim().isEmpty()) {
                    maxApplicants = Integer.parseInt(maxApplicantsStr.trim());
                }
            } catch (NumberFormatException ignored) {}

            Job job = new Job();
            job.setTitle(title.trim());
            job.setModuleCode(moduleCode.trim().toUpperCase());
            job.setModuleName(moduleName != null ? moduleName.trim() : "");
            job.setDescription(description != null ? description.trim() : "");
            job.setRequiredSkills(skills);
            job.setPostedBy(postedBy);
            job.setPostedByName(postedByName != null ? postedByName : "MO");
            job.setMaxApplicants(maxApplicants);
            job.setJobType(jobType != null && !jobType.isEmpty() ? jobType : "MODULE_TA");

            DataStorage storage = new DataStorage(getServletContext());
            storage.addJob(job);
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?success=1");
            return;
        }
        req.setAttribute("error", error);
        req.getRequestDispatcher("/mo/post-job.jsp").forward(req, resp);
    }
}

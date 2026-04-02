package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicantDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        String applicantId = req.getParameter("applicantId");
        if (applicantId == null || applicantId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=missing_applicant");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        List<Job> moJobs = storage.loadJobs().stream()
                .filter(j -> moId.equals(j.getPostedBy()))
                .collect(Collectors.toList());
        Set<String> moJobIds = moJobs.stream().map(Job::getId).collect(Collectors.toSet());

        List<Application> relatedApps = storage.loadApplications().stream()
                .filter(a -> applicantId.equals(a.getApplicantId()) && moJobIds.contains(a.getJobId()))
                .sorted(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .collect(Collectors.toList());

        if (relatedApps.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs?error=no_access");
            return;
        }

        Map<String, Job> jobMap = moJobs.stream().collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a));
        List<Object[]> rows = new ArrayList<>();
        int selected = 0;
        int pending = 0;
        int rejectedOrWithdrawn = 0;
        for (Application a : relatedApps) {
            rows.add(new Object[]{a, jobMap.get(a.getJobId())});
            if ("SELECTED".equals(a.getStatus())) selected++;
            else if ("PENDING".equals(a.getStatus())) pending++;
            else rejectedOrWithdrawn++;
        }

        TAProfile profile = storage.getProfileByUserId(applicantId);
        User user = storage.findUserById(applicantId);

        req.setAttribute("applicantUser", user);
        req.setAttribute("applicantProfile", profile);
        req.setAttribute("appRows", rows);
        req.setAttribute("selectedCount", selected);
        req.setAttribute("pendingCount", pending);
        req.setAttribute("otherCount", rejectedOrWithdrawn);
        req.getRequestDispatcher("/mo/applicant-detail.jsp").forward(req, resp);
    }
}

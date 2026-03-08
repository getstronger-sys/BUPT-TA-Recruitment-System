package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String applicantId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());

        List<Application> applications = storage.getApplicationsByApplicantId(applicantId);
        List<Job> allJobs = storage.loadJobs();
        Map<String, Job> jobMap = allJobs.stream().collect(Collectors.toMap(Job::getId, j -> j));

        List<Object[]> enriched = new ArrayList<>();
        for (Application a : applications) {
            Job j = jobMap.get(a.getJobId());
            enriched.add(new Object[]{a, j});
        }

        req.setAttribute("applications", enriched);
        req.getRequestDispatcher("/ta/applications.jsp").forward(req, resp);
    }
}

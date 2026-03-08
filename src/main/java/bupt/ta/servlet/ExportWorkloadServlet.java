package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ExportWorkloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataStorage storage = new DataStorage(getServletContext());
        List<Application> apps = storage.loadApplications().stream()
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .collect(Collectors.toList());
        Map<String, List<Application>> byApplicant = apps.stream().collect(Collectors.groupingBy(Application::getApplicantId));
        List<Job> allJobs = storage.loadJobs();
        Map<String, Job> jobMap = allJobs.stream().collect(Collectors.toMap(Job::getId, j -> j));
        List<User> users = storage.loadUsers();
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"ta_workload.csv\"");
        PrintWriter out = resp.getWriter();
        out.write("\uFEFF");
        out.println("TA Name,User ID,# Selected Jobs,Job Titles");
        for (Map.Entry<String, List<Application>> e : byApplicant.entrySet()) {
            User u = userMap.get(e.getKey());
            String name = u != null ? (u.getRealName() != null ? u.getRealName() : u.getUsername()) : e.getKey();
            String titles = e.getValue().stream()
                    .map(a -> {
                        Job j = jobMap.get(a.getJobId());
                        return j != null ? j.getTitle() : "Unknown";
                    })
                    .collect(Collectors.joining("; "));
            String csvName = "\"" + (name != null ? name.replace("\"", "\"\"") : "") + "\"";
            String csvTitles = "\"" + titles.replace("\"", "\"\"") + "\"";
            out.println(csvName + ",\"" + e.getKey() + "\"," + e.getValue().size() + "," + csvTitles);
        }
        out.flush();
    }
}

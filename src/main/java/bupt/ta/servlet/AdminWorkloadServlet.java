package bupt.ta.servlet;

import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.User;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin: Check TA's overall workload - number of selected jobs per TA.
 */
public class AdminWorkloadServlet extends HttpServlet {

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

        double avgWorkload = byApplicant.isEmpty() ? 0 : byApplicant.values().stream().mapToInt(List::size).average().orElse(0);

        List<Object[]> workloadRows = new ArrayList<>();
        for (Map.Entry<String, List<Application>> e : byApplicant.entrySet()) {
            User u = userMap.get(e.getKey());
            List<Application> selectedApps = e.getValue();
            List<String> jobTitles = selectedApps.stream()
                    .map(a -> {
                        Job j = jobMap.get(a.getJobId());
                        return j != null ? j.getTitle() : "Unknown";
                    })
                    .collect(Collectors.toList());
            int count = selectedApps.size();
            String name = u != null ? (u.getRealName() != null ? u.getRealName() : u.getUsername()) : e.getKey();
            boolean overloaded = count > avgWorkload;
            workloadRows.add(new Object[]{name, e.getKey(), count, jobTitles, overloaded});
        }
        workloadRows.sort((a, b) -> Integer.compare((Integer) b[2], (Integer) a[2])); // desc by count

        req.setAttribute("workloadRows", workloadRows);
        req.setAttribute("avgWorkload", avgWorkload);
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }
}

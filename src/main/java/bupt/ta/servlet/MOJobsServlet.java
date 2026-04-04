package bupt.ta.servlet;

import bupt.ta.ai.AIMatchService;
import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MOJobsServlet extends HttpServlet {

    private final AIMatchService aiService = new AIMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String moId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());

        List<Job> jobs = storage.loadJobs().stream()
                .filter(j -> moId.equals(j.getPostedBy()))
                .collect(Collectors.toList());

        List<Application> allApps = storage.loadApplications();
        Map<String, List<Application>> appsByJob = allApps.stream().collect(Collectors.groupingBy(Application::getJobId));

        List<Application> selectedApps = allApps.stream().filter(a -> "SELECTED".equals(a.getStatus())).collect(Collectors.toList());
        Map<String, Integer> workloadByTa = new HashMap<>();
        for (Application a : selectedApps) {
            workloadByTa.merge(a.getApplicantId(), 1, Integer::sum);
        }
        double avgWorkload = workloadByTa.isEmpty() ? 0 : workloadByTa.values().stream().mapToInt(Integer::intValue).average().orElse(0);

        List<TAProfile> profiles = storage.loadProfiles();
        Map<String, TAProfile> profileByUser = profiles.stream().collect(Collectors.toMap(TAProfile::getUserId, p -> p, (a, b) -> a));

        List<Object[]> enriched = new ArrayList<>();
        for (Job j : jobs) {
            List<Application> apps = appsByJob.getOrDefault(j.getId(), new ArrayList<>());
            List<AIMatchService.ApplicantRecommendation> all = new ArrayList<>();
            for (Application a : apps) {
                TAProfile profile = profileByUser.get(a.getApplicantId());
                AIMatchService.MatchResult match = aiService.matchSkills(profile, j);
                int workload = workloadByTa.getOrDefault(a.getApplicantId(), 0);
                boolean balanced = workload <= avgWorkload;
                all.add(new AIMatchService.ApplicantRecommendation(a, profile, match, workload, balanced));
            }
            List<AIMatchService.ApplicantRecommendation> pending = new ArrayList<>();
            List<AIMatchService.ApplicantRecommendation> interview = new ArrayList<>();
            List<AIMatchService.ApplicantRecommendation> withdrawn = new ArrayList<>();
            List<AIMatchService.ApplicantRecommendation> outcome = new ArrayList<>();
            for (AIMatchService.ApplicantRecommendation r : all) {
                String s = r.application.getStatus();
                if ("WITHDRAWN".equals(s)) {
                    withdrawn.add(r);
                } else if ("PENDING".equals(s)) {
                    pending.add(r);
                } else if ("INTERVIEW".equals(s)) {
                    interview.add(r);
                } else {
                    outcome.add(r);
                }
            }
            sortByMatch(pending);
            sortByMatch(interview);
            sortByMatch(withdrawn);
            sortByMatch(outcome);
            enriched.add(new Object[]{j, pending, interview, withdrawn, outcome});
        }

        req.setAttribute("jobsWithApps", enriched);
        req.getRequestDispatcher("/mo/jobs.jsp").forward(req, resp);
    }

    private static void sortByMatch(List<AIMatchService.ApplicantRecommendation> recs) {
        recs.sort((r1, r2) -> {
            int cmp = Double.compare(r2.matchResult.score, r1.matchResult.score);
            if (cmp != 0) return cmp;
            return Integer.compare(r1.currentWorkload, r2.currentWorkload);
        });
    }
}

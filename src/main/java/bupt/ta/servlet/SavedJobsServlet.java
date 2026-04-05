package bupt.ta.servlet;

import bupt.ta.ai.AIMatchService;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows TA saved jobs ordered by AI match score.
 */
public class SavedJobsServlet extends HttpServlet {

    private final AIMatchService aiService = new AIMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = (String) req.getSession().getAttribute("userId");
        DataStorage storage = new DataStorage(getServletContext());
        TAProfile profile = storage.getOrCreateProfile(userId);

        List<Object[]> jobsWithMatch = new ArrayList<>();
        for (String jobId : profile.getSavedJobIds()) {
            Job job = storage.getJobById(jobId);
            if (job == null) {
                continue;
            }
            AIMatchService.MatchResult match = aiService.matchSkills(profile, job);
            jobsWithMatch.add(new Object[]{job, match});
        }

        jobsWithMatch.sort((left, right) -> {
            AIMatchService.MatchResult leftMatch = (AIMatchService.MatchResult) left[1];
            AIMatchService.MatchResult rightMatch = (AIMatchService.MatchResult) right[1];
            int byScore = Double.compare(rightMatch.score, leftMatch.score);
            if (byScore != 0) {
                return byScore;
            }

            Job leftJob = (Job) left[0];
            Job rightJob = (Job) right[0];
            boolean leftOpen = "OPEN".equals(leftJob.getStatus());
            boolean rightOpen = "OPEN".equals(rightJob.getStatus());
            if (leftOpen != rightOpen) {
                return leftOpen ? -1 : 1;
            }

            String leftTitle = leftJob.getTitle() != null ? leftJob.getTitle() : "";
            String rightTitle = rightJob.getTitle() != null ? rightJob.getTitle() : "";
            return leftTitle.compareToIgnoreCase(rightTitle);
        });

        req.setAttribute("jobsWithMatch", jobsWithMatch);
        req.setAttribute("savedCount", jobsWithMatch.size());
        req.getRequestDispatcher("/ta/saved-jobs.jsp").forward(req, resp);
    }
}

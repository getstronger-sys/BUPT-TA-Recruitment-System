package bupt.ta.servlet;

import bupt.ta.ai.AIMatchService;
import bupt.ta.llm.DeepSeekClient;
import bupt.ta.llm.LlmMatchInsightService;
import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * On-demand AI applicant insight for an MO reviewing a candidate, paired with
 * {@code /mo/applicant-detail.jsp}. The insight job is chosen the same way as the
 * detail page (most recently applied job owned by this MO).
 */
public class MOMatchInsightServlet extends HttpServlet {

    private final AIMatchService aiService = new AIMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String moId = (String) req.getSession().getAttribute("userId");
        String applicantId = req.getParameter("applicantId");
        if (moId == null || moId.isEmpty() || applicantId == null || applicantId.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Set<String> moJobIds = storage.loadJobs().stream()
                .filter(j -> moId.equals(j.getPostedBy()))
                .map(Job::getId)
                .collect(Collectors.toSet());

        Application latest = storage.loadApplications().stream()
                .filter(a -> applicantId.trim().equals(a.getApplicantId()) && moJobIds.contains(a.getJobId()))
                .sorted(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .findFirst()
                .orElse(null);
        if (latest == null) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "You do not have access to this applicant.");
            return;
        }

        Job job = storage.getJobById(latest.getJobId());
        TAProfile profile = storage.getProfileByUserId(applicantId.trim());
        if (job == null || profile == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Applicant profile or job is not available.");
            return;
        }

        AIMatchService.MatchResult match = aiService.matchSkills(profile, job);
        DeepSeekClient client = DeepSeekClient.fromAdminSettings(storage.loadAiApiSettings());
        if (!client.isConfigured()) {
            writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "AI API is not configured. Ask an admin to enable it.");
            return;
        }
        String insight = new LlmMatchInsightService(client).buildInsight(profile, job, match);
        if (insight == null || insight.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_GATEWAY,
                    "AI did not return a response. Please try again later.");
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("ok", true);
        data.addProperty("insight", insight);
        resp.getWriter().write(data.toString());
    }

    private static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("ok", false);
        error.addProperty("error", message);
        resp.getWriter().write(error.toString());
    }
}

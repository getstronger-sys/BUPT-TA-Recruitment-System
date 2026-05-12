package bupt.ta.servlet;

import bupt.ta.ai.AIMatchService;
import bupt.ta.llm.DeepSeekClient;
import bupt.ta.llm.LlmMatchInsightService;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * On-demand AI match insight for a TA viewing a job. Invoked via fetch() from
 * {@code /ta/job-detail.jsp} when the TA clicks the "Generate AI insight" button.
 * Keeps the job detail page render fast (the synchronous DeepSeek call was removed
 * from {@code TAJobDetailServlet}).
 */
public class TAMatchInsightServlet extends HttpServlet {

    private final AIMatchService aiService = new AIMatchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String userId = (String) req.getSession().getAttribute("userId");
        String jobId = req.getParameter("jobId");
        if (userId == null || userId.isEmpty() || jobId == null || jobId.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        Job job = storage.getJobById(jobId.trim());
        if (job == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Job not found.");
            return;
        }

        TAProfile profile = storage.getOrCreateProfile(userId);
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

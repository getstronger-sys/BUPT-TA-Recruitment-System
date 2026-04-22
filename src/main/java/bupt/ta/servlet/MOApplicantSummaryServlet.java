package bupt.ta.servlet;

import bupt.ta.ai.AIMatchService;
import bupt.ta.llm.LlmApplicantSummaryService;
import bupt.ta.model.Application;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;
import bupt.ta.storage.DataStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * MO-only endpoint: generate AI summary for one application on demand.
 */
public class MOApplicantSummaryServlet extends HttpServlet {

    private final AIMatchService aiService = new AIMatchService();
    private final LlmApplicantSummaryService summaryService = new LlmApplicantSummaryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String moId = (String) req.getSession().getAttribute("userId");
        String applicationId = trim(req.getParameter("applicationId"));
        if (moId == null || moId.isEmpty() || applicationId.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
            return;
        }

        DataStorage storage = new DataStorage(getServletContext());
        List<Application> allApps = storage.loadApplications();
        Application target = null;
        for (Application app : allApps) {
            if (applicationId.equals(app.getId())) {
                target = app;
                break;
            }
        }
        if (target == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Application not found.");
            return;
        }

        Job job = storage.getJobById(target.getJobId());
        if (job == null || !moId.equals(job.getPostedBy())) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "You do not have access to this application.");
            return;
        }

        TAProfile profile = storage.getProfileByUserId(target.getApplicantId());
        AIMatchService.MatchResult match = aiService.matchSkills(profile, job);

        int currentWorkload = 0;
        int selectedTotal = 0;
        for (Application app : allApps) {
            if ("SELECTED".equals(app.getStatus())) {
                selectedTotal += 1;
                if (target.getApplicantId().equals(app.getApplicantId())) {
                    currentWorkload += 1;
                }
            }
        }
        long selectedApplicants = allApps.stream()
                .filter(a -> "SELECTED".equals(a.getStatus()))
                .map(Application::getApplicantId)
                .filter(id -> id != null && !id.trim().isEmpty())
                .distinct()
                .count();
        double avgWorkload = selectedApplicants == 0 ? 0 : (selectedTotal * 1.0 / selectedApplicants);
        boolean balanced = currentWorkload <= avgWorkload;

        List<String> lines = summaryService.buildSummaryLines(profile, job, match, currentWorkload, balanced);

        JsonObject data = new JsonObject();
        data.addProperty("ok", true);
        JsonArray arr = new JsonArray();
        for (String line : lines) {
            arr.add(line);
        }
        data.add("lines", arr);
        resp.getWriter().write(data.toString());
    }

    private static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("ok", false);
        error.addProperty("error", message);
        resp.getWriter().write(error.toString());
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

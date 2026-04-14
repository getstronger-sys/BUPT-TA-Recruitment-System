package bupt.ta.llm;

import bupt.ta.ai.AIMatchService;
import bupt.ta.model.Job;
import bupt.ta.model.TAProfile;

import java.io.IOException;

/**
 * Optional DeepSeek-generated narrative on top of rule-based {@link AIMatchService.MatchResult}.
 */
public final class LlmMatchInsightService {

    private final DeepSeekClient client;

    public LlmMatchInsightService() {
        this(new DeepSeekClient());
    }

    public LlmMatchInsightService(DeepSeekClient client) {
        this.client = client;
    }

    /**
     * @return short paragraph (English), or null if API unavailable / error
     */
    public String buildInsight(TAProfile profile, Job job, AIMatchService.MatchResult match) {
        if (!client.isConfigured() || job == null) {
            return null;
        }
        String sys = "You help teaching staff assess TA applicants. "
                + "Write 2-4 short sentences in English: strengths, gaps, and practical fit. "
                + "Be factual; do not invent credentials not implied by the data. "
                + "No bullet points; plain paragraph only.";

        StringBuilder data = new StringBuilder();
        data.append("Job title: ").append(nullToEmpty(job.getTitle())).append('\n');
        data.append("Module: ").append(nullToEmpty(job.getModuleCode())).append(' ')
                .append(nullToEmpty(job.getModuleName())).append('\n');
        data.append("Required skills: ").append(job.getRequiredSkills() != null ? String.join(", ", job.getRequiredSkills()) : "").append('\n');
        data.append("Responsibilities (excerpt): ")
                .append(trim(job.getResponsibilities(), 800)).append('\n');

        if (profile != null) {
            data.append("Applicant skills: ")
                    .append(profile.getSkills() != null ? String.join(", ", profile.getSkills()) : "unknown").append('\n');
            data.append("Programme: ").append(nullToEmpty(profile.getProgramme())).append('\n');
            data.append("TA experience (excerpt): ").append(trim(profile.getTaExperience(), 400)).append('\n');
        } else {
            data.append("Applicant profile: not available.\n");
        }

        if (match != null) {
            data.append("Rule-based match score: ").append(Math.round(match.score)).append("%\n");
            data.append("Matched skills: ").append(match.matched != null ? String.join(", ", match.matched) : "").append('\n');
            data.append("Missing skills: ").append(match.missing != null ? String.join(", ", match.missing) : "").append('\n');
        }

        try {
            return client.chat(sys, data.toString()).trim();
        } catch (IOException | IllegalStateException e) {
            return null;
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static String trim(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}

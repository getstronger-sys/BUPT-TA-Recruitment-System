package bupt.ta.util;

import bupt.ta.ai.AIMatchService;
import bupt.ta.model.Application;
import bupt.ta.model.InterviewEvaluation;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Sort and filter controls for MO applicant lists on the jobs management page.
 */
public final class MoApplicantListControls {

    public static final String SORT_MATCH_DESC = "match_desc";
    public static final String SORT_MATCH_ASC = "match_asc";
    public static final String SORT_EVAL_DESC = "eval_desc";
    public static final String SORT_EVAL_ASC = "eval_asc";
    public static final String SORT_REC = "rec";
    public static final String SORT_NAME_ASC = "name_asc";
    public static final String SORT_NAME_DESC = "name_desc";
    public static final String SORT_APPLIED_DESC = "applied_desc";
    public static final String SORT_APPLIED_ASC = "applied_asc";
    public static final String SORT_WORKLOAD_ASC = "workload_asc";
    public static final String SORT_WORKLOAD_DESC = "workload_desc";

    public static final String REC_ALL = "all";
    public static final String REC_NONE = "none";
    public static final String NOTICE_ALL = "all";
    public static final String NOTICE_SENT = "sent";
    public static final String NOTICE_UNSENT = "unsent";
    public static final String OUTCOME_ALL = "all";

    private static final Set<String> VALID_SORTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            SORT_MATCH_DESC, SORT_MATCH_ASC, SORT_EVAL_DESC, SORT_EVAL_ASC, SORT_REC,
            SORT_NAME_ASC, SORT_NAME_DESC, SORT_APPLIED_DESC, SORT_APPLIED_ASC,
            SORT_WORKLOAD_ASC, SORT_WORKLOAD_DESC
    )));

    private static final Map<String, Integer> REC_RANK = Map.of(
            "STRONG_HIRE", 0,
            "HIRE", 1,
            "WAITLIST", 2,
            "REJECT", 3
    );

    private final String sort;
    private final String recommendation;
    private final String notice;
    private final String outcomeStatus;
    private final Integer evalMin;

    public MoApplicantListControls(String sort, String recommendation, String notice,
                                   String outcomeStatus, Integer evalMin) {
        this.sort = sort;
        this.recommendation = recommendation;
        this.notice = notice;
        this.outcomeStatus = outcomeStatus;
        this.evalMin = evalMin;
    }

    public static MoApplicantListControls fromRequest(HttpServletRequest req) {
        String view = resolveView(req);
        String defaultSort = "outcome".equals(view) ? SORT_NAME_ASC : SORT_EVAL_DESC;
        String sort = normalize(req.getParameter("moSort"), defaultSort);
        if (!VALID_SORTS.contains(sort)) {
            sort = defaultSort;
        }
        String rec = normalize(req.getParameter("moRec"), REC_ALL).toUpperCase(Locale.ROOT);
        if (!REC_ALL.equalsIgnoreCase(rec) && !REC_NONE.equals(rec) && !REC_RANK.containsKey(rec)) {
            rec = REC_ALL;
        }
        String notice = normalize(req.getParameter("moNotice"), NOTICE_ALL).toLowerCase(Locale.ROOT);
        if (!NOTICE_ALL.equals(notice) && !NOTICE_SENT.equals(notice) && !NOTICE_UNSENT.equals(notice)) {
            notice = NOTICE_ALL;
        }
        String outcome = normalize(req.getParameter("moOutcome"), OUTCOME_ALL).toUpperCase(Locale.ROOT);
        if (!OUTCOME_ALL.equalsIgnoreCase(outcome)
                && !"SELECTED".equals(outcome) && !"REJECTED".equals(outcome)
                && !"AUTO_CLOSED".equals(outcome) && !"WITHDRAWN".equals(outcome)) {
            outcome = OUTCOME_ALL;
        }
        Integer evalMin = parseEvalMin(req.getParameter("moEvalMin"));
        if ("outcome".equals(view)) {
            sort = SORT_NAME_ASC;
            rec = REC_ALL;
            notice = NOTICE_ALL;
            evalMin = null;
        }
        return new MoApplicantListControls(sort, rec, notice, outcome, evalMin);
    }

    private static String resolveView(HttpServletRequest req) {
        String view = req.getParameter("view");
        if (view == null || view.isEmpty()) {
            Object viewAttr = req.getAttribute("moJobsView");
            if (viewAttr != null) {
                view = viewAttr.toString();
            }
        }
        return view != null ? view : "";
    }

    public String getSort() { return sort; }
    public String getRecommendation() { return recommendation; }
    public String getNotice() { return notice; }
    public String getOutcomeStatus() { return outcomeStatus; }
    public Integer getEvalMin() { return evalMin; }

    public boolean hasActiveFilters(String view) {
        if (evalMin != null && usesInterviewEvaluationFilters(view)) {
            return true;
        }
        if ("outcome".equals(view)) {
            return !OUTCOME_ALL.equalsIgnoreCase(outcomeStatus);
        }
        if ("interview".equals(view)) {
            if (!NOTICE_ALL.equals(notice)) return true;
        }
        if ("pending".equals(view) || "withdrawn".equals(view)) {
            return false;
        }
        if (!REC_ALL.equalsIgnoreCase(recommendation)) return true;
        return false;
    }

    private static boolean usesInterviewEvaluationFilters(String view) {
        return "interview".equals(view) || "waitlist".equals(view);
    }

    /** Sort/filter toolbar applies to waitlist and outcomes only. */
    public static boolean usesListToolbar(String view) {
        return "waitlist".equals(view) || "outcome".equals(view);
    }

    public String toQueryString() {
        return toQueryString(null);
    }

    public String toQueryString(String view) {
        StringBuilder q = new StringBuilder();
        if (!"outcome".equals(view)) {
            append(q, "moSort", sort);
        }
        if (!REC_ALL.equalsIgnoreCase(recommendation)) {
            append(q, "moRec", recommendation);
        }
        if (evalMin != null) {
            append(q, "moEvalMin", String.valueOf(evalMin));
        }
        if (!NOTICE_ALL.equals(notice)) {
            append(q, "moNotice", notice);
        }
        if (!OUTCOME_ALL.equalsIgnoreCase(outcomeStatus)) {
            append(q, "moOutcome", outcomeStatus);
        }
        return q.toString();
    }

    public static int listIndexForView(String view) {
        switch (view) {
            case "interview": return 2;
            case "waitlist": return 3;
            case "withdrawn": return 4;
            case "outcome": return 5;
            default: return 1;
        }
    }

    @SuppressWarnings("unchecked")
    public static void applyToJobRow(Object[] row, String view,
                                     Map<String, InterviewEvaluation> evaluationByApplicationId,
                                     MoApplicantListControls controls) {
        if (row == null || controls == null) {
            return;
        }
        int idx = listIndexForView(view);
        List<AIMatchService.ApplicantRecommendation> source =
                (List<AIMatchService.ApplicantRecommendation>) row[idx];
        if (source == null) {
            row[idx] = Collections.emptyList();
            return;
        }
        row[idx] = controls.filterAndSort(new ArrayList<>(source), evaluationByApplicationId, view);
    }

    public List<AIMatchService.ApplicantRecommendation> filterAndSort(
            List<AIMatchService.ApplicantRecommendation> source,
            Map<String, InterviewEvaluation> evaluationByApplicationId,
            String view) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<AIMatchService.ApplicantRecommendation> list = new ArrayList<>(source);
        list.removeIf(r -> !matchesFilters(r, evaluationByApplicationId, view));
        list.sort(buildComparator(evaluationByApplicationId));
        return list;
    }

    private boolean matchesFilters(AIMatchService.ApplicantRecommendation r,
                                   Map<String, InterviewEvaluation> evaluationByApplicationId,
                                   String view) {
        if ("outcome".equals(view)) {
            if (!OUTCOME_ALL.equalsIgnoreCase(outcomeStatus)
                    && !outcomeStatus.equals(r.application.getStatus())) {
                return false;
            }
            return true;
        }
        if ("withdrawn".equals(view)) {
            return true;
        }
        if ("pending".equals(view)) {
            return true;
        }
        if ("interview".equals(view)) {
            if (!matchesNoticeFilter(r)) {
                return false;
            }
        }
        return matchesEvaluationFilters(r, evaluationByApplicationId, true);
    }

    private boolean matchesEvaluationFilters(AIMatchService.ApplicantRecommendation r,
                                             Map<String, InterviewEvaluation> evaluationByApplicationId,
                                             boolean includeRecommendation) {
        InterviewEvaluation ev = evaluationByApplicationId != null
                ? evaluationByApplicationId.get(r.application.getId()) : null;
        if (evalMin != null) {
            if (ev == null || ev.getTotalScore() < evalMin) {
                return false;
            }
        }
        if (includeRecommendation && !REC_ALL.equalsIgnoreCase(recommendation)) {
            if (REC_NONE.equalsIgnoreCase(recommendation)) {
                if (ev != null) return false;
            } else if (ev == null || ev.getRecommendation() == null
                    || !recommendation.equalsIgnoreCase(ev.getRecommendation())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesNoticeFilter(AIMatchService.ApplicantRecommendation r) {
        Application a = r.application;
        boolean hasNotice = (a.getInterviewTime() != null && !a.getInterviewTime().trim().isEmpty())
                || (a.getInterviewLocation() != null && !a.getInterviewLocation().trim().isEmpty())
                || (a.getInterviewAssessment() != null && !a.getInterviewAssessment().trim().isEmpty());
        if (NOTICE_SENT.equals(notice)) {
            return hasNotice;
        }
        if (NOTICE_UNSENT.equals(notice)) {
            return !hasNotice;
        }
        return true;
    }

    private Comparator<AIMatchService.ApplicantRecommendation> buildComparator(
            Map<String, InterviewEvaluation> evaluationByApplicationId) {
        final Map<String, InterviewEvaluation> evals = evaluationByApplicationId;
        Comparator<AIMatchService.ApplicantRecommendation> c;
        switch (sort) {
            case SORT_MATCH_ASC:
                c = Comparator.comparingDouble(
                        (AIMatchService.ApplicantRecommendation r) -> r.matchResult.score);
                break;
            case SORT_EVAL_DESC:
                c = Comparator.<AIMatchService.ApplicantRecommendation>comparingInt(
                        r -> evalScore(r, evals)).reversed();
                break;
            case SORT_EVAL_ASC:
                c = Comparator.comparingInt(
                        (AIMatchService.ApplicantRecommendation r) -> evalScoreAsc(r, evals));
                break;
            case SORT_REC:
                c = Comparator.<AIMatchService.ApplicantRecommendation>comparingInt(
                                r -> recRank(r, evals))
                        .thenComparingInt(r -> -evalScore(r, evals));
                break;
            case SORT_NAME_ASC:
                c = Comparator.comparing(
                        (AIMatchService.ApplicantRecommendation r) -> applicantName(r),
                        String.CASE_INSENSITIVE_ORDER);
                break;
            case SORT_NAME_DESC:
                c = Comparator.comparing(
                        (AIMatchService.ApplicantRecommendation r) -> applicantName(r),
                        String.CASE_INSENSITIVE_ORDER).reversed();
                break;
            case SORT_APPLIED_ASC:
                c = Comparator.comparing(
                        (AIMatchService.ApplicantRecommendation r) -> appliedAt(r),
                        Comparator.nullsLast(String::compareTo));
                break;
            case SORT_APPLIED_DESC:
                c = Comparator.comparing(
                        (AIMatchService.ApplicantRecommendation r) -> appliedAt(r),
                        Comparator.nullsLast(String::compareTo)).reversed();
                break;
            case SORT_WORKLOAD_ASC:
                c = Comparator.comparingInt(
                        (AIMatchService.ApplicantRecommendation r) -> r.currentWorkload);
                break;
            case SORT_WORKLOAD_DESC:
                c = Comparator.<AIMatchService.ApplicantRecommendation>comparingInt(
                        r -> r.currentWorkload).reversed();
                break;
            case SORT_MATCH_DESC:
            default:
                c = Comparator.<AIMatchService.ApplicantRecommendation>comparingDouble(
                                r -> r.matchResult.score).reversed()
                        .thenComparingInt(r -> r.currentWorkload);
                break;
        }
        return c.thenComparing(
                (AIMatchService.ApplicantRecommendation r) -> r.application.getId());
    }

    private static int evalScore(AIMatchService.ApplicantRecommendation r,
                                 Map<String, InterviewEvaluation> evaluationByApplicationId) {
        InterviewEvaluation ev = evaluationByApplicationId != null
                ? evaluationByApplicationId.get(r.application.getId()) : null;
        return ev != null ? ev.getTotalScore() : Integer.MIN_VALUE;
    }

    private static int evalScoreAsc(AIMatchService.ApplicantRecommendation r,
                                    Map<String, InterviewEvaluation> evaluationByApplicationId) {
        int score = evalScore(r, evaluationByApplicationId);
        return score == Integer.MIN_VALUE ? Integer.MAX_VALUE : score;
    }

    private static int recRank(AIMatchService.ApplicantRecommendation r,
                               Map<String, InterviewEvaluation> evaluationByApplicationId) {
        InterviewEvaluation ev = evaluationByApplicationId != null
                ? evaluationByApplicationId.get(r.application.getId()) : null;
        if (ev == null || ev.getRecommendation() == null) {
            return 99;
        }
        return REC_RANK.getOrDefault(ev.getRecommendation().toUpperCase(Locale.ROOT), 50);
    }

    private static String applicantName(AIMatchService.ApplicantRecommendation r) {
        String name = r.application.getApplicantName();
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return r.application.getApplicantId() != null ? r.application.getApplicantId() : "";
    }

    private static String appliedAt(AIMatchService.ApplicantRecommendation r) {
        return r.application.getAppliedAt() != null ? r.application.getAppliedAt() : "";
    }

    private static Integer parseEvalMin(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < 0) return 0;
            if (v > 100) return 100;
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static void append(StringBuilder q, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (q.length() > 0) {
            q.append('&');
        }
        q.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}

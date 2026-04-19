package bupt.ta.util;

import bupt.ta.model.Job;
import bupt.ta.model.WorkArrangementItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds derived Job text fields from structured work arrangements and recalculates TA slots.
 */
public final class WorkArrangementSupport {

    private WorkArrangementSupport() {
    }

    private static String trim(String s) {
        return s != null ? s.trim() : "";
    }

    /**
     * Parses work-arrangement rows from the MO posting / edit form (same field names as post-job.jsp).
     */
    public static List<WorkArrangementItem> parseWorkRowsFromRequest(HttpServletRequest req) {
        String[] names = req.getParameterValues("waWorkName");
        if (names == null || names.length == 0) {
            return new ArrayList<>();
        }
        String[] sessionDurs = req.getParameterValues("waSessionDuration");
        String[] occCounts = req.getParameterValues("waOccurrenceCount");
        String[] taCounts = req.getParameterValues("waTaCount");
        String[] times = req.getParameterValues("waSpecificTime");
        List<WorkArrangementItem> out = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String n = trim(names[i]);
            String sd = sessionDurs != null && i < sessionDurs.length ? trim(sessionDurs[i]) : "";
            int oc = 0;
            if (occCounts != null && i < occCounts.length) {
                try {
                    oc = Integer.parseInt(trim(occCounts[i]));
                } catch (NumberFormatException ignored) {
                }
            }
            int tc = 0;
            if (taCounts != null && i < taCounts.length) {
                try {
                    tc = Integer.parseInt(trim(taCounts[i]));
                } catch (NumberFormatException ignored) {
                }
            }
            String t = times != null && i < times.length ? trim(times[i]) : "";
            if (n.isEmpty() && sd.isEmpty() && oc <= 0 && tc <= 0 && t.isEmpty()) {
                continue;
            }
            WorkArrangementItem it = new WorkArrangementItem();
            it.setWorkName(n);
            it.setSessionDuration(sd);
            it.setOccurrenceCount(oc);
            it.setTaCount(tc);
            it.setSpecificTime(t);
            out.add(it);
        }
        return out;
    }

    /**
     * @return null if valid, otherwise a short English message for the MO
     */
    public static String validateWorkRowsForPosting(List<WorkArrangementItem> items) {
        if (items.isEmpty()) {
            return "Add at least one work arrangement row (work name, per-session duration, occurrences, and TA count are required).";
        }
        for (int i = 0; i < items.size(); i++) {
            WorkArrangementItem it = items.get(i);
            if (trim(it.getWorkName()).isEmpty()) {
                return "Work arrangement row " + (i + 1) + ": work name is required.";
            }
            if (trim(it.getResolvedSessionDuration()).isEmpty()) {
                return "Work arrangement row " + (i + 1) + ": per-session duration is required.";
            }
            if (it.getOccurrenceCount() < 1) {
                return "Work arrangement row " + (i + 1) + ": number of occurrences must be at least 1.";
            }
            if (it.getTaCount() < 1) {
                return "Work arrangement row " + (i + 1) + ": TA count must be at least 1.";
            }
        }
        return null;
    }

    /**
     * Sets {@link Job#setTaSlots}, {@link Job#setWorkArrangements}, workingHours, workload, taAllocationPlan from items.
     */
    public static void applyDerivedFields(Job job, List<WorkArrangementItem> items) {
        int sum = items.stream().mapToInt(WorkArrangementItem::getTaCount).sum();
        job.setTaSlots(Math.max(1, sum));
        job.setWorkArrangements(new ArrayList<>(items));
        StringBuilder wh = new StringBuilder();
        StringBuilder wl = new StringBuilder();
        StringBuilder plan = new StringBuilder();
        for (WorkArrangementItem it : items) {
            String timeLine = trim(it.getSpecificTime());
            if (timeLine.isEmpty()) {
                timeLine = "TBD — to be scheduled based on operational needs";
            }
            if (wh.length() > 0) {
                wh.append('\n');
            }
            wh.append(it.getWorkName()).append(": ").append(timeLine);
            if (wl.length() > 0) {
                wl.append("; ");
            }
            wl.append(it.getWorkName()).append(" ")
                    .append(it.getResolvedSessionDuration())
                    .append(" × ")
                    .append(it.getResolvedOccurrenceCount())
                    .append(" occurrence(s)");
            if (plan.length() > 0) {
                plan.append('\n');
            }
            plan.append("- ").append(it.getWorkName())
                    .append(" | per session: ").append(it.getResolvedSessionDuration())
                    .append(" | occurrences: ").append(it.getResolvedOccurrenceCount())
                    .append(" | TAs: ").append(it.getTaCount());
            if (!trim(it.getSpecificTime()).isEmpty()) {
                plan.append(" | time: ").append(it.getSpecificTime());
            } else {
                plan.append(" | time: TBD (to be arranged as needed)");
            }
        }
        job.setWorkingHours(wh.toString());
        job.setWorkload(wl.toString());
        job.setTaAllocationPlan(plan.toString());
    }
}

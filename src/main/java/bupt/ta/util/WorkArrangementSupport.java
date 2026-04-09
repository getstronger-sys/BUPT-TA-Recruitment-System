package bupt.ta.util;

import bupt.ta.model.Job;
import bupt.ta.model.WorkArrangementItem;

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

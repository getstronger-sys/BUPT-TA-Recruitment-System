package bupt.ta.util;

import bupt.ta.model.Application;
import bupt.ta.model.Job;

import java.util.List;
import java.util.Objects;

/**
 * Shared helpers for enforcing selected-TA capacity on a posting.
 */
public final class JobSelectionCapacity {

    private JobSelectionCapacity() {
    }

    public static int selectionSlots(Job job) {
        if (job == null) {
            return 1;
        }
        return job.getTaSlots() > 0 ? job.getTaSlots() : 1;
    }

    public static long selectedCount(List<Application> apps, String jobId, String excludeApplicationId) {
        if (apps == null || jobId == null || jobId.trim().isEmpty()) {
            return 0;
        }
        return apps.stream()
                .filter(app -> Objects.equals(jobId, app.getJobId()))
                .filter(app -> !Objects.equals(excludeApplicationId, app.getId()))
                .filter(app -> "SELECTED".equals(app.getStatus()))
                .count();
    }

    public static boolean hasVacancy(Job job, List<Application> apps, String excludeApplicationId) {
        return selectedCount(apps, job != null ? job.getId() : null, excludeApplicationId) < selectionSlots(job);
    }
}

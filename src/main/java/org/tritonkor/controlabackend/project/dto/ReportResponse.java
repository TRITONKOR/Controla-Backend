package org.tritonkor.controlabackend.project.dto;

import org.tritonkor.controlabackend.project.entity.RiskLevel;
import org.tritonkor.controlabackend.task.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID projectId,
        String projectTitle,

        int totalTasks,
        int doneTasks,
        int inProgressTasks,
        int reviewTasks,
        int toDoTasks,

        int donePercent,

        LocalDateTime deadline,
        long daysToDeadline,
        boolean overdue,

        int activeTasks,
        int reviewShare,

        RiskLevel riskLevel,

        List<StatusDistributionItem> statusDistribution
) {

    public record StatusDistributionItem(
            TaskStatus status,
            int count,
            int percent
    ) {
    }
}

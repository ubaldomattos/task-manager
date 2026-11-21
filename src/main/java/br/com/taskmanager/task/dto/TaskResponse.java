package br.com.taskmanager.task.dto;

import br.com.taskmanager.task.domain.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Representação de uma tarefa devolvida pela API. */
public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate scheduledDate,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getScheduledDate(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}

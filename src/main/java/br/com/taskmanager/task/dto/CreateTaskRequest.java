package br.com.taskmanager.task.dto;

import br.com.taskmanager.task.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dados para cadastrar uma tarefa. */
public record CreateTaskRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = Task.TITLE_MAX_LENGTH, message = "O título deve ter no máximo {max} caracteres.")
        String title,

        @Size(max = Task.DESCRIPTION_MAX_LENGTH, message = "A descrição deve ter no máximo {max} caracteres.")
        String description,

        @NotNull(message = "A data da tarefa é obrigatória.")
        LocalDate scheduledDate) {
}

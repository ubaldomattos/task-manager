package br.com.taskmanager.task.dto;

import br.com.taskmanager.task.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para editar uma tarefa. A data programada não pode ser alterada. */
public record UpdateTaskRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = Task.TITLE_MAX_LENGTH, message = "O título deve ter no máximo {max} caracteres.")
        String title,

        @Size(max = Task.DESCRIPTION_MAX_LENGTH, message = "A descrição deve ter no máximo {max} caracteres.")
        String description) {
}

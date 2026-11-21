package br.com.taskmanager.task.dto;

import jakarta.validation.constraints.NotNull;

/** Nova situação de conclusão de uma tarefa. */
public record CompletionRequest(
        @NotNull(message = "Informe se a tarefa está concluída.")
        Boolean completed) {
}

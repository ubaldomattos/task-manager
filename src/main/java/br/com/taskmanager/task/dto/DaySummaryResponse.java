package br.com.taskmanager.task.dto;

import br.com.taskmanager.task.repository.DailyTaskCount;

import java.time.LocalDate;

/**
 * Resumo das tarefas de um dia, usado pelo calendário para marcar os dias.
 *
 * @param date    dia
 * @param total   quantidade de tarefas do dia
 * @param pending quantidade de tarefas ainda não concluídas
 */
public record DaySummaryResponse(LocalDate date, long total, long pending) {

    public static DaySummaryResponse from(DailyTaskCount count) {
        return new DaySummaryResponse(count.date(), count.total(), count.pending());
    }
}

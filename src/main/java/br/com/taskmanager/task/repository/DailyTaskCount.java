package br.com.taskmanager.task.repository;

import java.time.LocalDate;

/** Projeção com a contagem de tarefas de um dia. */
public record DailyTaskCount(LocalDate date, Long total, Long pending) {
}

package br.com.taskmanager.task.service;

import br.com.taskmanager.task.dto.CreateTaskRequest;
import br.com.taskmanager.task.dto.DaySummaryResponse;
import br.com.taskmanager.task.dto.TaskResponse;
import br.com.taskmanager.task.dto.UpdateTaskRequest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Casos de uso do gerenciamento de tarefas. */
public interface TaskService {

    List<TaskResponse> findByDate(LocalDate date);

    List<DaySummaryResponse> summarizeMonth(YearMonth month);

    TaskResponse create(CreateTaskRequest request);

    TaskResponse update(long id, UpdateTaskRequest request);

    TaskResponse changeCompletion(long id, boolean completed);

    void delete(long id);
}

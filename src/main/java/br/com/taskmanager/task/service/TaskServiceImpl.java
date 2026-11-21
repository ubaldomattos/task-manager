package br.com.taskmanager.task.service;

import br.com.taskmanager.task.domain.Task;
import br.com.taskmanager.task.dto.CreateTaskRequest;
import br.com.taskmanager.task.dto.DaySummaryResponse;
import br.com.taskmanager.task.dto.TaskResponse;
import br.com.taskmanager.task.dto.UpdateTaskRequest;
import br.com.taskmanager.task.exception.TaskNotFoundException;
import br.com.taskmanager.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository repository;

    public TaskServiceImpl(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TaskResponse> findByDate(LocalDate date) {
        return repository.findByScheduledDateOrderByCompletedAscIdAsc(date).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public List<DaySummaryResponse> summarizeMonth(YearMonth month) {
        return repository.countByDayBetween(month.atDay(1), month.atEndOfMonth()).stream()
                .map(DaySummaryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Task task = repository.save(new Task(request.title(), request.description(), request.scheduledDate()));
        log.info("Tarefa {} criada para {}", task.getId(), task.getScheduledDate());
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse update(long id, UpdateTaskRequest request) {
        Task task = findTask(id);
        task.updateDetails(request.title(), request.description());
        repository.flush();
        log.info("Tarefa {} atualizada", id);
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse changeCompletion(long id, boolean completed) {
        Task task = findTask(id);
        task.changeCompletion(completed);
        repository.flush();
        log.info("Tarefa {} marcada como {}", id, completed ? "concluída" : "pendente");
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Task task = findTask(id);
        repository.delete(task);
        log.info("Tarefa {} excluída", id);
    }

    private Task findTask(long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}

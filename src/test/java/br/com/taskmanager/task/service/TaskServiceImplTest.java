package br.com.taskmanager.task.service;

import br.com.taskmanager.task.domain.Task;
import br.com.taskmanager.task.dto.CreateTaskRequest;
import br.com.taskmanager.task.dto.DaySummaryResponse;
import br.com.taskmanager.task.dto.TaskResponse;
import br.com.taskmanager.task.dto.UpdateTaskRequest;
import br.com.taskmanager.task.exception.InvalidTaskException;
import br.com.taskmanager.task.exception.TaskNotFoundException;
import br.com.taskmanager.task.repository.DailyTaskCount;
import br.com.taskmanager.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskServiceImpl service;

    @Test
    void criaTarefaPendenteNaDataInformada() {
        when(repository.save(any(Task.class))).thenAnswer(call -> withId(call.getArgument(0), 7L));

        TaskResponse response = service.create(new CreateTaskRequest(" Estudar ", "Capítulo 3", DATE));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTitle()).isEqualTo("Estudar");
        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.scheduledDate()).isEqualTo(DATE);
        assertThat(response.completed()).isFalse();
    }

    @Test
    void naoSalvaTarefaInvalida() {
        assertThatThrownBy(() -> service.create(new CreateTaskRequest("  ", null, DATE)))
                .isInstanceOf(InvalidTaskException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void listaTarefasDoDia() {
        when(repository.findByScheduledDateOrderByCompletedAscIdAsc(DATE))
                .thenReturn(List.of(withId(new Task("A", null, DATE), 1L), withId(new Task("B", null, DATE), 2L)));

        List<TaskResponse> tasks = service.findByDate(DATE);

        assertThat(tasks).extracting(TaskResponse::title).containsExactly("A", "B");
    }

    @Test
    void resumeOMesDoPrimeiroAoUltimoDia() {
        YearMonth february = YearMonth.of(2024, 2);
        LocalDate first = LocalDate.of(2024, 2, 1);
        LocalDate last = LocalDate.of(2024, 2, 29);
        when(repository.countByDayBetween(first, last))
                .thenReturn(List.of(new DailyTaskCount(LocalDate.of(2024, 2, 5), 3L, 1L)));

        List<DaySummaryResponse> summary = service.summarizeMonth(february);

        assertThat(summary).containsExactly(new DaySummaryResponse(LocalDate.of(2024, 2, 5), 3, 1));
    }

    @Test
    void editaTituloEDescricao() {
        Task task = withId(new Task("Antigo", "desc", DATE), 3L);
        when(repository.findById(3L)).thenReturn(Optional.of(task));

        TaskResponse response = service.update(3L, new UpdateTaskRequest("Novo", "nova desc"));

        assertThat(response.title()).isEqualTo("Novo");
        assertThat(response.description()).isEqualTo("nova desc");
        assertThat(response.scheduledDate()).isEqualTo(DATE);
        verify(repository).flush();
    }

    @Test
    void marcaComoConcluidaEDepoisComoPendente() {
        Task task = withId(new Task("Tarefa", null, DATE), 4L);
        when(repository.findById(4L)).thenReturn(Optional.of(task));

        assertThat(service.changeCompletion(4L, true).completed()).isTrue();
        assertThat(service.changeCompletion(4L, false).completed()).isFalse();
    }

    @Test
    void excluiTarefaExistente() {
        Task task = withId(new Task("Tarefa", null, DATE), 5L);
        when(repository.findById(5L)).thenReturn(Optional.of(task));

        service.delete(5L);

        verify(repository).delete(task);
    }

    @Test
    void falhaAoOperarTarefaInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UpdateTaskRequest("x", null)))
                .isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> service.changeCompletion(99L, true))
                .isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
        verify(repository, never()).delete(any());
    }

    private static Task withId(Task task, long id) {
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}

package br.com.taskmanager;

import br.com.taskmanager.support.TestDatabase;
import br.com.taskmanager.task.dto.CreateTaskRequest;
import br.com.taskmanager.task.dto.TaskResponse;
import br.com.taskmanager.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica a criação automática do banco e a persistência dos dados entre reinicializações. */
class DatabasePersistenceTest {

    private static final LocalDate DATE = LocalDate.of(2025, 12, 24);

    @Test
    void criaOBancoNaPrimeiraExecucaoEMantemOsDadosAoReiniciar() {
        Path database = TestDatabase.newPath();
        assertThat(database.getParent()).doesNotExist();

        long id;
        try (ConfigurableApplicationContext first = start(database)) {
            assertThat(database).exists();
            TaskService service = first.getBean(TaskService.class);
            id = service.create(new CreateTaskRequest("Ceia de Natal", null, DATE)).id();
            service.changeCompletion(id, true);
        }

        try (ConfigurableApplicationContext second = start(database)) {
            List<TaskResponse> tasks = second.getBean(TaskService.class).findByDate(DATE);

            assertThat(tasks).singleElement().satisfies(task -> {
                assertThat(task.id()).isEqualTo(id);
                assertThat(task.title()).isEqualTo("Ceia de Natal");
                assertThat(task.completed()).isTrue();
            });
        }
        assertThat(Files.isRegularFile(database)).isTrue();
    }

    private static ConfigurableApplicationContext start(Path database) {
        return new SpringApplicationBuilder(TaskManagerApplication.class)
                .web(WebApplicationType.NONE)
                .run("--taskmanager.database.path=" + database);
    }
}

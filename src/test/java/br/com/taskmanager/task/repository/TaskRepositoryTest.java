package br.com.taskmanager.task.repository;

import br.com.taskmanager.config.DatabaseConfig;
import br.com.taskmanager.support.TestDatabase;
import br.com.taskmanager.task.domain.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Testes de persistência contra um arquivo SQLite real, com o esquema criado pelo Flyway. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DatabaseConfig.class)
class TaskRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2025, 3, 10);

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("taskmanager.database.path", () -> TestDatabase.newPath().toString());
    }

    @Autowired
    private TaskRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void salvaTarefaComIdentificadorEDatasDeAuditoria() {
        Task saved = repository.saveAndFlush(new Task("Consulta médica", "Levar exames", DAY));
        entityManager.clear();

        Task found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isPositive();
        assertThat(found.getTitle()).isEqualTo("Consulta médica");
        assertThat(found.getDescription()).isEqualTo("Levar exames");
        assertThat(found.getScheduledDate()).isEqualTo(DAY);
        assertThat(found.isCompleted()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isEqualTo(found.getCreatedAt());
    }

    @Test
    void gravaDatasComoTextoIsoEConclusaoComoInteiro() {
        Task task = new Task("Pagar conta", null, DAY);
        task.markCompleted();
        Long id = repository.saveAndFlush(task).getId();

        Map<String, Object> row = jdbc.queryForMap(
                "select scheduled_date, completed, created_at from tasks where id = ?", id);

        assertThat(row.get("scheduled_date")).isEqualTo("2025-03-10");
        assertThat(((Number) row.get("completed")).intValue()).isEqualTo(1);
        assertThat((String) row.get("created_at")).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
    }

    @Test
    void atualizacaoPersisteConclusaoEAlteraDataDeAtualizacao() throws InterruptedException {
        Task task = repository.saveAndFlush(new Task("Revisar texto", null, DAY));
        var createdAt = task.getCreatedAt();

        Thread.sleep(5);
        task.markCompleted();
        repository.saveAndFlush(task);
        entityManager.clear();

        Task found = repository.findById(task.getId()).orElseThrow();
        assertThat(found.isCompleted()).isTrue();
        assertThat(found.getCreatedAt()).isEqualTo(createdAt);
        assertThat(found.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    void listaTarefasDoDiaComPendentesPrimeiro() {
        Task done = new Task("Concluída", null, DAY);
        done.markCompleted();
        repository.save(done);
        repository.save(new Task("Pendente 1", null, DAY));
        repository.save(new Task("Outro dia", null, DAY.plusDays(1)));
        repository.save(new Task("Pendente 2", null, DAY));
        repository.flush();

        List<Task> tasks = repository.findByScheduledDateOrderByCompletedAscIdAsc(DAY);

        assertThat(tasks).extracting(Task::getTitle)
                .containsExactly("Pendente 1", "Pendente 2", "Concluída");
    }

    @Test
    void contaTarefasPorDiaDentroDoIntervalo() {
        repository.save(new Task("Antes do mês", null, LocalDate.of(2025, 2, 28)));
        repository.save(new Task("Dia 1 - a", null, LocalDate.of(2025, 3, 1)));
        Task done = new Task("Dia 1 - b", null, LocalDate.of(2025, 3, 1));
        done.markCompleted();
        repository.save(done);
        repository.save(new Task("Dia 31", null, LocalDate.of(2025, 3, 31)));
        repository.save(new Task("Depois do mês", null, LocalDate.of(2025, 4, 1)));
        repository.flush();

        List<DailyTaskCount> counts = repository.countByDayBetween(
                LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));

        assertThat(counts).containsExactly(
                new DailyTaskCount(LocalDate.of(2025, 3, 1), 2L, 1L),
                new DailyTaskCount(LocalDate.of(2025, 3, 31), 1L, 1L));
    }

    @Test
    void excluiTarefa() {
        Task task = repository.saveAndFlush(new Task("Excluir", null, DAY));

        repository.delete(task);
        repository.flush();

        assertThat(repository.findById(task.getId())).isEmpty();
    }
}

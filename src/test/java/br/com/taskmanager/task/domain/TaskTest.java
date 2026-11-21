package br.com.taskmanager.task.domain;

import br.com.taskmanager.task.exception.InvalidTaskException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);

    @Nested
    @DisplayName("Criação")
    class Creation {

        @Test
        void criaTarefaPendenteComDadosNormalizados() {
            Task task = new Task("  Comprar pão  ", "  na padaria  ", DATE);

            assertThat(task.getTitle()).isEqualTo("Comprar pão");
            assertThat(task.getDescription()).isEqualTo("na padaria");
            assertThat(task.getScheduledDate()).isEqualTo(DATE);
            assertThat(task.isCompleted()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        void descricaoVaziaEhTratadaComoAusente(String description) {
            Task task = new Task("Título", description, DATE);

            assertThat(task.getDescription()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void rejeitaTituloVazio(String title) {
            assertThatThrownBy(() -> new Task(title, null, DATE))
                    .isInstanceOf(InvalidTaskException.class)
                    .hasMessageContaining("título");
        }

        @Test
        void rejeitaTituloAcimaDoLimite() {
            String title = "a".repeat(Task.TITLE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new Task(title, null, DATE))
                    .isInstanceOf(InvalidTaskException.class);
        }

        @Test
        void aceitaTituloNoLimite() {
            String title = "a".repeat(Task.TITLE_MAX_LENGTH);

            assertThat(new Task(title, null, DATE).getTitle()).hasSize(Task.TITLE_MAX_LENGTH);
        }

        @Test
        void rejeitaDescricaoAcimaDoLimite() {
            String description = "a".repeat(Task.DESCRIPTION_MAX_LENGTH + 1);

            assertThatThrownBy(() -> new Task("Título", description, DATE))
                    .isInstanceOf(InvalidTaskException.class);
        }

        @Test
        void rejeitaDataAusente() {
            assertThatThrownBy(() -> new Task("Título", null, null))
                    .isInstanceOf(InvalidTaskException.class)
                    .hasMessageContaining("data");
        }
    }

    @Nested
    @DisplayName("Edição e conclusão")
    class Changes {

        @Test
        void editaTituloEDescricaoSemAlterarData() {
            Task task = new Task("Antigo", "desc", DATE);

            task.updateDetails(" Novo ", " ");

            assertThat(task.getTitle()).isEqualTo("Novo");
            assertThat(task.getDescription()).isNull();
            assertThat(task.getScheduledDate()).isEqualTo(DATE);
        }

        @Test
        void edicaoInvalidaNaoAlteraATarefa() {
            Task task = new Task("Original", "desc", DATE);

            assertThatThrownBy(() -> task.updateDetails(" ", "outra"))
                    .isInstanceOf(InvalidTaskException.class);
            assertThat(task.getTitle()).isEqualTo("Original");
            assertThat(task.getDescription()).isEqualTo("desc");
        }

        @Test
        void descricaoInvalidaNaoAlteraOTitulo() {
            Task task = new Task("Original", "desc", DATE);
            String longDescription = "a".repeat(Task.DESCRIPTION_MAX_LENGTH + 1);

            assertThatThrownBy(() -> task.updateDetails("Novo", longDescription))
                    .isInstanceOf(InvalidTaskException.class);
            assertThat(task.getTitle()).isEqualTo("Original");
        }

        @Test
        void alternaEntreConcluidaEPendente() {
            Task task = new Task("Título", null, DATE);

            task.changeCompletion(true);
            assertThat(task.isCompleted()).isTrue();

            task.changeCompletion(false);
            assertThat(task.isCompleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Datas de auditoria")
    class Auditing {

        @Test
        void preencheCriacaoEAtualizacaoAoPersistir() {
            Task task = new Task("Título", null, DATE);

            task.onCreate();

            assertThat(task.getCreatedAt()).isNotNull();
            assertThat(task.getUpdatedAt()).isEqualTo(task.getCreatedAt());
        }

        @Test
        void atualizacaoAlteraSomenteDataDeAtualizacao() throws InterruptedException {
            Task task = new Task("Título", null, DATE);
            task.onCreate();
            var createdAt = task.getCreatedAt();

            Thread.sleep(5);
            task.onUpdate();

            assertThat(task.getCreatedAt()).isEqualTo(createdAt);
            assertThat(task.getUpdatedAt()).isAfter(createdAt);
        }
    }
}

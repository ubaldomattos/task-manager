package br.com.taskmanager.task.controller;

import br.com.taskmanager.task.dto.CreateTaskRequest;
import br.com.taskmanager.task.dto.DaySummaryResponse;
import br.com.taskmanager.task.dto.TaskResponse;
import br.com.taskmanager.task.dto.UpdateTaskRequest;
import br.com.taskmanager.task.exception.InvalidTaskException;
import br.com.taskmanager.task.exception.TaskNotFoundException;
import br.com.taskmanager.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final LocalDate DATE = LocalDate.of(2025, 3, 10);
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 1, 9, 30);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskService service;

    @Test
    void listaTarefasDoDia() throws Exception {
        when(service.findByDate(DATE)).thenReturn(List.of(task(1L, "Reunião", false)));

        mvc.perform(get("/api/tasks").param("date", "2025-03-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Reunião"))
                .andExpect(jsonPath("$[0].scheduledDate").value("2025-03-10"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void exigeDataNaListagem() throws Exception {
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("O parâmetro 'date' é obrigatório."));
    }

    @Test
    void rejeitaDataMalFormada() throws Exception {
        mvc.perform(get("/api/tasks").param("date", "10/03/2025"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O valor do parâmetro 'date' é inválido."));
    }

    @Test
    void resumeOMes() throws Exception {
        when(service.summarizeMonth(YearMonth.of(2025, 3)))
                .thenReturn(List.of(new DaySummaryResponse(DATE, 2, 1)));

        mvc.perform(get("/api/tasks/summary").param("month", "2025-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2025-03-10"))
                .andExpect(jsonPath("$[0].total").value(2))
                .andExpect(jsonPath("$[0].pending").value(1));
    }

    @Test
    void rejeitaMesInvalido() throws Exception {
        mvc.perform(get("/api/tasks/summary").param("month", "2025-13"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criaTarefa() throws Exception {
        when(service.create(any(CreateTaskRequest.class))).thenReturn(task(5L, "Estudar", false));

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Estudar", "description": "Spring", "scheduledDate": "2025-03-10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));

        verify(service).create(new CreateTaskRequest("Estudar", "Spring", DATE));
    }

    @Test
    void rejeitaCadastroSemTituloESemData() throws Exception {
        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.title").value("O título é obrigatório."))
                .andExpect(jsonPath("$.errors.scheduledDate").value("A data da tarefa é obrigatória."));

        verifyNoInteractions(service);
    }

    @Test
    void rejeitaTituloLongo() throws Exception {
        String title = "a".repeat(121);

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + title + "\", \"scheduledDate\": \"2025-03-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("O título deve ter no máximo 120 caracteres."));
    }

    @Test
    void rejeitaJsonMalFormado() throws Exception {
        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
    }

    @Test
    void converteRegraDeNegocioVioladaEmErro400() throws Exception {
        when(service.create(any())).thenThrow(new InvalidTaskException("O título da tarefa é obrigatório."));

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"x\", \"scheduledDate\": \"2025-03-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O título da tarefa é obrigatório."));
    }

    @Test
    void editaTarefa() throws Exception {
        when(service.update(eq(5L), any(UpdateTaskRequest.class))).thenReturn(task(5L, "Novo", false));

        mvc.perform(put("/api/tasks/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Novo\", \"description\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Novo"));

        verify(service).update(5L, new UpdateTaskRequest("Novo", null));
    }

    @Test
    void marcaTarefaComoConcluida() throws Exception {
        when(service.changeCompletion(5L, true)).thenReturn(task(5L, "Tarefa", true));

        mvc.perform(patch("/api/tasks/5/completion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void exigeSituacaoNaConclusao() throws Exception {
        mvc.perform(patch("/api/tasks/5/completion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.completed").exists());

        verifyNoInteractions(service);
    }

    @Test
    void excluiTarefa() throws Exception {
        mvc.perform(delete("/api/tasks/5"))
                .andExpect(status().isNoContent());

        verify(service).delete(5L);
    }

    @Test
    void retorna404ParaTarefaInexistente() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(service).delete(anyLong());

        mvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Tarefa não encontrada"))
                .andExpect(jsonPath("$.detail").value("Tarefa 99 não encontrada."));
    }

    @Test
    void ocultaDetalhesDeErrosInesperados() throws Exception {
        when(service.findByDate(any())).thenThrow(new IllegalStateException("falha interna com detalhes"));

        mvc.perform(get("/api/tasks").param("date", "2025-03-10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente em instantes."));
    }

    private static TaskResponse task(long id, String title, boolean completed) {
        return new TaskResponse(id, title, null, DATE, completed, NOW, NOW);
    }
}

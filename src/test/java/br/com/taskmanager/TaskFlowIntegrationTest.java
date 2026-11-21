package br.com.taskmanager;

import br.com.taskmanager.support.TestDatabase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo completo pela API, com a aplicação inteira e um arquivo SQLite real. */
@SpringBootTest
@AutoConfigureMockMvc
class TaskFlowIntegrationTest {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("taskmanager.database.path", () -> TestDatabase.newPath().toString());
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void cadastraEditaConcluiEExcluiTarefa() throws Exception {
        // Cadastro
        String created = mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Ir ao mercado", "description": "Leite e café", "scheduledDate": "2025-06-15"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode task = json.readTree(created);
        long id = task.get("id").asLong();

        mvc.perform(get("/api/tasks").param("date", "2025-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Ir ao mercado"))
                .andExpect(jsonPath("$[0].completed").value(false));

        mvc.perform(get("/api/tasks/summary").param("month", "2025-06"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date").value("2025-06-15"))
                .andExpect(jsonPath("$[0].total").value(1))
                .andExpect(jsonPath("$[0].pending").value(1));

        // Edição
        mvc.perform(put("/api/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Ir ao supermercado\", \"description\": \"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ir ao supermercado"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.scheduledDate").value("2025-06-15"));

        // Conclusão
        mvc.perform(patch("/api/tasks/{id}/completion", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mvc.perform(get("/api/tasks").param("date", "2025-06-15"))
                .andExpect(jsonPath("$[0].completed").value(true));
        mvc.perform(get("/api/tasks/summary").param("month", "2025-06"))
                .andExpect(jsonPath("$[0].pending").value(0));

        // Exclusão
        mvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks").param("date", "2025-06-15"))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(delete("/api/tasks/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void serveAInterfaceWeb() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}

package br.com.taskmanager.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Configuração do arquivo SQLite, definida em {@code taskmanager.database.*}.
 *
 * @param path caminho do arquivo do banco; pode ser relativo ao diretório de execução
 */
@Validated
@ConfigurationProperties(prefix = "taskmanager.database")
public record DatabaseProperties(@NotNull Path path) {

    public Path absolutePath() {
        return path.toAbsolutePath().normalize();
    }
}

package br.com.taskmanager.support;

import java.nio.file.Path;
import java.util.UUID;

/** Gera caminhos de arquivos SQLite isolados para os testes, dentro de {@code target/}. */
public final class TestDatabase {

    private static final Path ROOT = Path.of("target", "test-databases");

    private TestDatabase() {
    }

    /** Caminho de um banco novo, em um subdiretório que ainda não existe. */
    public static Path newPath() {
        return ROOT.resolve(UUID.randomUUID().toString()).resolve("taskmanager-test.db");
    }
}

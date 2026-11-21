package br.com.taskmanager.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cria o {@link javax.sql.DataSource} do SQLite.
 *
 * <p>O driver SQLite cria o arquivo do banco, mas não cria os diretórios
 * intermediários. Esta configuração garante que o diretório exista antes da
 * primeira conexão, de modo que o banco seja criado na primeira execução.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(DatabaseProperties properties) {
        Path databaseFile = properties.absolutePath();
        createParentDirectory(databaseFile);
        log.info("Banco de dados SQLite: {}", databaseFile);

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.sqlite.JDBC")
                .url("jdbc:sqlite:" + databaseFile)
                .build();
    }

    private static void createParentDirectory(Path databaseFile) {
        Path directory = databaseFile.getParent();
        if (directory == null || Files.isDirectory(directory)) {
            return;
        }
        try {
            Files.createDirectories(directory);
            log.info("Diretório do banco de dados criado: {}", directory);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Não foi possível criar o diretório do banco de dados: " + directory, e);
        }
    }
}

package br.com.taskmanager.task.domain;

import br.com.taskmanager.shared.persistence.LocalDateTextConverter;
import br.com.taskmanager.shared.persistence.LocalDateTimeTextConverter;
import br.com.taskmanager.task.exception.InvalidTaskException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Tarefa programada para uma data.
 *
 * <p>A entidade protege as próprias regras: o título é obrigatório e sem
 * espaços nas bordas, a descrição vazia é tratada como ausente e a data
 * programada é definida na criação e não muda depois.</p>
 */
@Entity
@Table(name = "tasks")
public class Task {

    public static final int TITLE_MAX_LENGTH = 120;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "description", length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Convert(converter = LocalDateTextConverter.class)
    @Column(name = "scheduled_date", nullable = false, updatable = false)
    private LocalDate scheduledDate;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Convert(converter = LocalDateTimeTextConverter.class)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Convert(converter = LocalDateTimeTextConverter.class)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Exigido pela JPA. */
    protected Task() {
    }

    public Task(String title, String description, LocalDate scheduledDate) {
        if (scheduledDate == null) {
            throw new InvalidTaskException("A data da tarefa é obrigatória.");
        }
        this.scheduledDate = scheduledDate;
        this.completed = false;
        updateDetails(title, description);
    }

    /** Altera título e descrição, aplicando as mesmas regras da criação. */
    public void updateDetails(String title, String description) {
        String normalizedTitle = normalizeTitle(title);
        String normalizedDescription = normalizeDescription(description);
        this.title = normalizedTitle;
        this.description = normalizedDescription;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void markPending() {
        this.completed = false;
    }

    public void changeCompletion(boolean completed) {
        if (completed) {
            markCompleted();
        } else {
            markPending();
        }
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = now();
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private static String normalizeTitle(String title) {
        String normalized = title == null ? "" : title.strip();
        if (normalized.isEmpty()) {
            throw new InvalidTaskException("O título da tarefa é obrigatório.");
        }
        if (normalized.length() > TITLE_MAX_LENGTH) {
            throw new InvalidTaskException(
                    "O título deve ter no máximo " + TITLE_MAX_LENGTH + " caracteres.");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalized = description.strip();
        if (normalized.length() > DESCRIPTION_MAX_LENGTH) {
            throw new InvalidTaskException(
                    "A descrição deve ter no máximo " + DESCRIPTION_MAX_LENGTH + " caracteres.");
        }
        return normalized;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

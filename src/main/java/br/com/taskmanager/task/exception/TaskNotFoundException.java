package br.com.taskmanager.task.exception;

/** Indica que não existe tarefa com o identificador informado. */
public class TaskNotFoundException extends RuntimeException {

    private final long taskId;

    public TaskNotFoundException(long taskId) {
        super("Tarefa " + taskId + " não encontrada.");
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}

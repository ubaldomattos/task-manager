package br.com.taskmanager.task.exception;

/** Indica que os dados de uma tarefa violam uma regra de negócio. */
public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}

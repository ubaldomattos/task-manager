package br.com.taskmanager.shared.web;

import br.com.taskmanager.task.exception.InvalidTaskException;
import br.com.taskmanager.task.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas no formato Problem Details (RFC 9457),
 * com mensagens em português que a interface pode exibir diretamente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
        log.warn("Tarefa não encontrada: {}", ex.getTaskId());
        return problem(HttpStatus.NOT_FOUND, "Tarefa não encontrada", ex.getMessage());
    }

    @ExceptionHandler(InvalidTaskException.class)
    public ProblemDetail handleInvalidTask(InvalidTaskException ex) {
        log.warn("Dados de tarefa inválidos: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        String detail = String.join(" ", errors.values());
        log.warn("Requisição rejeitada pela validação: {}", errors.keySet());

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Dados inválidos", detail);
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().headers(headers).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.warn("Corpo da requisição ilegível: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().headers(headers).body(problem(HttpStatus.BAD_REQUEST,
                "Requisição inválida", "O corpo da requisição está mal formado ou contém valores inválidos."));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return ResponseEntity.badRequest().headers(headers).body(problem(HttpStatus.BAD_REQUEST,
                "Requisição inválida", "O parâmetro '" + ex.getParameterName() + "' é obrigatório."));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String name = ex.getPropertyName() != null ? ex.getPropertyName() : "informado";
        return ResponseEntity.badRequest().headers(headers).body(problem(HttpStatus.BAD_REQUEST,
                "Requisição inválida", "O valor do parâmetro '" + name + "' é inválido."));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}

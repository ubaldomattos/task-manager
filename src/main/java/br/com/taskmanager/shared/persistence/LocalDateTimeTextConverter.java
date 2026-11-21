package br.com.taskmanager.shared.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Grava {@link LocalDateTime} como texto ISO-8601 com precisão fixa de
 * milissegundos ({@code yyyy-MM-dd'T'HH:mm:ss.SSS}).
 *
 * <p>A precisão fixa garante que a ordenação do texto corresponda à
 * ordenação cronológica.</p>
 */
@Converter
public class LocalDateTimeTextConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public String convertToDatabaseColumn(LocalDateTime value) {
        return value == null ? null : FORMAT.format(value);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String value) {
        return value == null ? null : LocalDateTime.parse(value, FORMAT);
    }
}

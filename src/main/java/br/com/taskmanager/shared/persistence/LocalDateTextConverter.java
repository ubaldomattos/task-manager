package br.com.taskmanager.shared.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

/**
 * Grava {@link LocalDate} como texto ISO-8601 ({@code yyyy-MM-dd}).
 *
 * <p>O SQLite não possui tipo de data nativo. O formato ISO mantém o valor
 * legível e preserva a ordenação lexicográfica, permitindo consultas por
 * intervalo com {@code BETWEEN}.</p>
 */
@Converter
public class LocalDateTextConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}

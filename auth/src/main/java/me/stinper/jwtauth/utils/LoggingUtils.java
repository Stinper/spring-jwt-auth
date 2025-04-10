package me.stinper.jwtauth.utils;

import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

public final class LoggingUtils {
    private LoggingUtils() {}

    public static String logFieldErrorsList(@NonNull List<FieldError> fieldErrors, @NonNull String delimiter) {
        return fieldErrors.stream()
                .map(fe -> "['" + fe.getField() + "', " + fe.getCode() + ", " + fe.getDefaultMessage() + "]")
                .collect(Collectors.joining(delimiter));
    }

    public static String logFieldErrorsListCommaSeparated(@NonNull List<FieldError> fieldErrors) {
        return logFieldErrorsList(fieldErrors, ", ");
    }

    public static String logFieldErrorsListLineSeparated(@NonNull List<FieldError> fieldErrors) {
        return logFieldErrorsList(fieldErrors, "\n");
    }
}

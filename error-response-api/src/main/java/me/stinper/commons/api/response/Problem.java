package me.stinper.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Базовый класс, который представляет собой простейшее сообщение об ошибке,
 * состоящее из машиночитаемого типа ошибки и человекочитаемого
 * сообщения об ошибке.
 */

@Data
@RequiredArgsConstructor
public class Problem {
    private final String type;

    @JsonProperty(value = "localized_message")
    private final String localizedMessage;
}

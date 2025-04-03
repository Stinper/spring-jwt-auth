package me.stinper.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Класс, представляющий собой ошибку в параметре запроса. Под "параметром" подразумевается
 * любой компонент запроса, такой как заголовок, Query Parameter и т.д.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RequestParameterProblem extends Problem {
    @JsonProperty(value = "parameter_name")
    private final String parameterName;

    public RequestParameterProblem(String type, String localizedMessage, String parameterName) {
        super(type, localizedMessage);
        this.parameterName = parameterName;
    }
}

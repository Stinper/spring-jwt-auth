package me.stinper.commons.api.response.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FieldCodeValidationProblemDetails extends FieldValidationProblemDetails {
    private final String code;

    public FieldCodeValidationProblemDetails(String message,
                                             String field,
                                             String code) {
        super(message, field);
        this.code = code;
    }
}

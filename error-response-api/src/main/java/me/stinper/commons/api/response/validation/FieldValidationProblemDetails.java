package me.stinper.commons.api.response.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class FieldValidationProblemDetails extends RequestBodyValidationProblemDetails {
    private final String field;

    public FieldValidationProblemDetails(String message,
                                         String field) {
        super(message);
        this.field = field;
    }
}

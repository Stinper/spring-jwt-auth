package me.stinper.commons.api.response.beanvalidation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.validation.FieldValidationProblemDetails;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ConstraintViolationProblemDetails extends FieldValidationProblemDetails {
    private final ConstraintViolationDetails constraints;

    public ConstraintViolationProblemDetails(String field,
                                             String message,
                                             ConstraintViolationDetails constraints) {
        super(message, field);
        this.constraints = constraints;
    }
}

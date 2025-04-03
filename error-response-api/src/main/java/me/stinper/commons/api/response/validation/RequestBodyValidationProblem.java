package me.stinper.commons.api.response.validation;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.Problem;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class RequestBodyValidationProblem extends Problem {
    private final List<? extends RequestBodyValidationProblemDetails> details;

    public RequestBodyValidationProblem(String type,
                                        String localizedMessage,
                                        List<? extends RequestBodyValidationProblemDetails> details) {
        super(type, localizedMessage);
        this.details = details;
    }
}


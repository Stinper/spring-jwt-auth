package me.stinper.commons.api.response.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ValueValidationProblemDetails extends RequestBodyValidationProblemDetails {
    @JsonProperty(value = "error_code")
    private final String errorCode;

    public ValueValidationProblemDetails(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

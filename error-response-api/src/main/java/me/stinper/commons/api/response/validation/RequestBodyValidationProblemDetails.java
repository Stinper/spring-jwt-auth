package me.stinper.commons.api.response.validation;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RequestBodyValidationProblemDetails {
    private final String message;
}

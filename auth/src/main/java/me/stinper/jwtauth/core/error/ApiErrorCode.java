package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApiErrorCode {
    INVALID_INPUT_CONSTRAINT_VIOLATIONS_FOUND("invalid-input.constraint-violations-found"),
    INVALID_INPUT_VALIDATION_ERROR("invalid-input.validation-error"),

    INTERNAL_SERVER_ERROR("internal-server-error"),
    NOT_FOUND("not-found");

    private final String code;
}

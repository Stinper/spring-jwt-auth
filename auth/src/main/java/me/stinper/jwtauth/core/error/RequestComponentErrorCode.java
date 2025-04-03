package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequestComponentErrorCode {
    REQUIRED_HEADER_MISSED("request.components.header.required-header-missed"),

    REQUEST_PARAMETER_TYPE_MISMATCH("request.components.parameter.type-mismatch"),

    UNRESOLVED_PROPERTY("request.components.query-params.unresolved-property");

    private final String code;

}

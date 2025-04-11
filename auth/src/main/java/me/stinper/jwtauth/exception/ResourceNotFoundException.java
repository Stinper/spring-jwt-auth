package me.stinper.jwtauth.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String errorMessageCode;
    private Object[] args;

    public ResourceNotFoundException(String errorMessageCode, Object... args) {
        this.errorMessageCode = errorMessageCode;
        this.args = args;
    }

    public ResourceNotFoundException(String errorMessageCode) {
        this.errorMessageCode = errorMessageCode;
    }
}

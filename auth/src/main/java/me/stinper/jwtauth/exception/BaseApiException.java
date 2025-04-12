package me.stinper.jwtauth.exception;

import lombok.Getter;

import java.io.Serial;
import java.util.Arrays;

public abstract class BaseApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -521548369012463471L;

    @Getter
    private final String errorMessageCode;
    private Object[] args;

    public BaseApiException(String errorMessageCode, Object... args) {
        this.errorMessageCode = errorMessageCode;
        this.args = args;
    }

    public BaseApiException(String errorMessageCode) {
        this.errorMessageCode = errorMessageCode;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}

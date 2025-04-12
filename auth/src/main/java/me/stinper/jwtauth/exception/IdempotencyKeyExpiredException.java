package me.stinper.jwtauth.exception;

import java.io.Serial;

public class IdempotencyKeyExpiredException extends BaseApiException {
    @Serial
    private static final long serialVersionUID = -3884697611303224092L;

    public IdempotencyKeyExpiredException(String errorMessageCode, Object... args) {
        super(errorMessageCode, args);
    }

    public IdempotencyKeyExpiredException(String errorMessageCode) {
        super(errorMessageCode);
    }
}

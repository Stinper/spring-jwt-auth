package me.stinper.jwtauth.exception;


import java.io.Serial;

public class ResourceNotFoundException extends BaseApiException {
    @Serial
    private static final long serialVersionUID = 8367612520779492958L;

    public ResourceNotFoundException(String errorMessageCode, Object... args) {
        super(errorMessageCode, args);
    }

    public ResourceNotFoundException(String errorMessageCode) {
        super(errorMessageCode);
    }
}

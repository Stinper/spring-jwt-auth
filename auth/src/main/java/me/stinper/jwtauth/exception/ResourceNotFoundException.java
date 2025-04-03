package me.stinper.jwtauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    private final String localizedMessage;

    public ResourceNotFoundException(String localizedMessage, Throwable cause) {
        super(cause);
        this.localizedMessage = localizedMessage;
    }
}

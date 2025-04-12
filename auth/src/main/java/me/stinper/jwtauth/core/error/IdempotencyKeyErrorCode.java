package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum IdempotencyKeyErrorCode {
    KEY_IS_EXPIRED("idempotency-keys.expired");

    private final String code;
}

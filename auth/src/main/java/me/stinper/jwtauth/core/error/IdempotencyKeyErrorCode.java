package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum IdempotencyKeyErrorCode {
    KEY_NOT_UNIQUE("idempotency-keys.key-not-unique");

    private final String code;
}

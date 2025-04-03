package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthenticationErrorCode {
    BAD_CREDENTIALS("authentication.bad-credentials"),
    ACCOUNT_DISABLED("authentication.account-disabled"),
    ACCOUNT_LOCKED("authentication.account-locked"),
    ACCOUNT_NOT_FOUND("authentication.account-not-found"),

    EXPIRED_JWT("authentication.jwt.expired"),
    MALFORMED_JWT("authentication.jwt.malformed"),
    UNSUPPORTED_JWT("authentication.jwt.unsupported"),
    COMMON_JWT_ERROR("authentication.jwt.common-error");

    private final String code;
}

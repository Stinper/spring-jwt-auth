package me.stinper.jwtauth.core.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserErrorCode {
    EMAIL_NOT_UNIQUE("users.email.not-unique"),
    WRONG_PASSWORD("users.password.wrong"),
    OLD_AND_NEW_PASSWORDS_MATCHES("users.password.old-and-new-password-matches"),
    PASSWORDS_DO_NOT_MATCH("users.password.passwords-do-not-match");

    private final String code;
}

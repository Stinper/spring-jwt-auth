package me.stinper.jwtauth.exception.handler;

import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.Problem;
import me.stinper.commons.api.response.ProblemKind;
import me.stinper.jwtauth.core.Headers;
import me.stinper.jwtauth.core.error.AuthenticationErrorCode;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class AuthenticationExceptionsHandler {
    private final MessageSourceHelper messageSourceHelper;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Problem> handleBadCredentialsException(BadCredentialsException e) {
        return this.buildAuthenticationErrorResponse(
                HttpStatus.UNAUTHORIZED,
                AuthenticationErrorCode.BAD_CREDENTIALS.getCode(),
                "messages.authentication.bad-credentials"
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Problem> handleLockedException(LockedException e) {
        return this.buildAuthenticationErrorResponse(
                HttpStatus.UNAUTHORIZED,
                AuthenticationErrorCode.ACCOUNT_LOCKED.getCode(),
                "messages.authentication.account-locked"
        );

    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Problem> handleDisabledException(DisabledException e) {
        return this.buildAuthenticationErrorResponse(
                HttpStatus.UNAUTHORIZED,
                AuthenticationErrorCode.ACCOUNT_DISABLED.getCode(),
                "messages.authentication.account-disabled"
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Problem> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return this.buildAuthenticationErrorResponse(
                HttpStatus.NOT_FOUND,
                AuthenticationErrorCode.ACCOUNT_NOT_FOUND.getCode(),
                "messages.authentication.account-not-found"
        );

    }

    private ResponseEntity<Problem> buildAuthenticationErrorResponse(@NonNull HttpStatus status,
                                                                     @NonNull String errorType,
                                                                     @NonNull String errorMessageCode) {
        return ResponseEntity
                .status(status)
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.AUTHENTICATION_ERROR.getKind())
                .body(
                        new Problem(
                                errorType,
                                messageSourceHelper.getLocalizedMessage(errorMessageCode)
                        )
                );
    }
}

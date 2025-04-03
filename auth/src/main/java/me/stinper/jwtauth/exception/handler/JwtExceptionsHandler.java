package me.stinper.jwtauth.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.Problem;
import me.stinper.commons.api.response.ProblemKind;
import me.stinper.jwtauth.core.Headers;
import me.stinper.jwtauth.core.error.AuthenticationErrorCode;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@RequiredArgsConstructor
public class JwtExceptionsHandler {
    private final MessageSourceHelper messageSourceHelper;

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Problem> handleExpiredJwtException(ExpiredJwtException e) {
        return this.buildJwtErrorResponse(AuthenticationErrorCode.EXPIRED_JWT.getCode(), "messages.authentication.jwt.expired");
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<Problem> handleUnsupportedJwtException(UnsupportedJwtException e) {
        return this.buildJwtErrorResponse(AuthenticationErrorCode.UNSUPPORTED_JWT.getCode(), "messages.authentication.jwt.unsupported");
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Problem> handleMalformedJwtException(MalformedJwtException e) {
        return this.buildJwtErrorResponse(AuthenticationErrorCode.MALFORMED_JWT.getCode(), "messages.authentication.jwt.malformed");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Problem> handleJwtException(JwtException e) {
        return this.buildJwtErrorResponse(AuthenticationErrorCode.COMMON_JWT_ERROR.getCode(), "messages.authentication.jwt.common-error");
    }

    private ResponseEntity<Problem> buildJwtErrorResponse(@NonNull String errorType, @NonNull String errorMessageCode) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.AUTHENTICATION_ERROR.getKind())
                .body(
                        new Problem(
                                errorType,
                                this.messageSourceHelper.getLocalizedMessage(errorMessageCode)
                        )
                );
    }
}

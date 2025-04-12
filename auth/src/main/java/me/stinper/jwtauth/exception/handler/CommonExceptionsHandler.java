package me.stinper.jwtauth.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.commons.api.response.Problem;
import me.stinper.jwtauth.core.error.ApiErrorCode;
import me.stinper.jwtauth.core.error.IdempotencyKeyErrorCode;
import me.stinper.jwtauth.exception.IdempotencyKeyExpiredException;
import me.stinper.jwtauth.exception.RelatedEntityExistsException;
import me.stinper.jwtauth.exception.ResourceNotFoundException;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CommonExceptionsHandler {
    private final MessageSourceHelper messageSourceHelper;

    @ExceptionHandler(RelatedEntityExistsException.class)
    public ResponseEntity<Problem> handleRelatedEntityExistsException(RelatedEntityExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new Problem(e.getErrorCode(), messageSourceHelper.getLocalizedMessage(e.getMessageCode()))
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Problem> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new Problem(
                                ApiErrorCode.NOT_FOUND.getCode(),
                                messageSourceHelper.getLocalizedMessage(e.getErrorMessageCode(), e.getArgs())
                        )
                );
    }

    @ExceptionHandler(IdempotencyKeyExpiredException.class)
    public ResponseEntity<Problem> handleIdempotencyKeyExpiredException(IdempotencyKeyExpiredException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new Problem(
                                IdempotencyKeyErrorCode.KEY_IS_EXPIRED.getCode(),
                                messageSourceHelper.getLocalizedMessage(e.getErrorMessageCode(), e.getArgs())
                        )
                );
    }


    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Problem> handleJsonProcessingException(JsonProcessingException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new Problem(
                                ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                                messageSourceHelper.getLocalizedMessage("messages.internal.internal-server-error")
                        )
                );
    }

}

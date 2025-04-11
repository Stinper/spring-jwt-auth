package me.stinper.jwtauth.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stinper.commons.api.response.Problem;
import me.stinper.jwtauth.core.error.ApiErrorCode;
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

}

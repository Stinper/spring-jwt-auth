package me.stinper.jwtauth.exception.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.ProblemKind;
import me.stinper.commons.api.response.beanvalidation.AbstractConstraintViolationsErrorResponseBuilder;
import me.stinper.commons.api.response.validation.FieldCodeValidationProblemDetails;
import me.stinper.commons.api.response.validation.RequestBodyValidationProblem;
import me.stinper.commons.api.response.validation.ValueValidationProblemDetails;
import me.stinper.jwtauth.core.Headers;
import me.stinper.jwtauth.core.error.ApiErrorCode;
import me.stinper.jwtauth.exception.EntityValidationException;
import me.stinper.jwtauth.exception.ObjectValueValidationException;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@RequiredArgsConstructor
public class ValidationExceptionsHandler {
    private final MessageSourceHelper messageSourceHelper;
    private final AbstractConstraintViolationsErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RequestBodyValidationProblem> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity
                .unprocessableEntity()
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.VALIDATION_ERROR.getKind())
                .body(
                        new RequestBodyValidationProblem(
                                ApiErrorCode.INVALID_INPUT_CONSTRAINT_VIOLATIONS_FOUND.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                        "messages.validation.errors-found",
                                        e.getConstraintViolations().size()
                                ),
                                errorResponseBuilder.buildErrorResponseDetailsFromConstraintViolations(e.getConstraintViolations())
                        )
                );
    }

    @ExceptionHandler(ObjectValueValidationException.class)
    public ResponseEntity<?> handleObjectValueValidationException(ObjectValueValidationException e) {
        return ResponseEntity
                .unprocessableEntity()
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.VALIDATION_ERROR.getKind())
                .body(
                        new RequestBodyValidationProblem(
                                ApiErrorCode.INVALID_INPUT_VALIDATION_ERROR.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                        "messages.validation.errors-found",
                                        e.getObjectErrors().size()
                                ),
                                e.getObjectErrors().stream().map(ValidationExceptionsHandler::handleObjectError).toList()
                        )
                );
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<?> handleEntityValidationException(EntityValidationException e) {
        return ResponseEntity
                .unprocessableEntity()
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.VALIDATION_ERROR.getKind())
                .body(
                        new RequestBodyValidationProblem(
                                ApiErrorCode.INVALID_INPUT_VALIDATION_ERROR.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                    "messages.validation.errors-found",
                                    e.getFieldErrors().size()
                                ),
                                e.getFieldErrors().stream().map(ValidationExceptionsHandler::handleFieldError).toList()
                        )
                );
    }


    private static ValueValidationProblemDetails handleObjectError(@NonNull ObjectError objectError) {
        return new ValueValidationProblemDetails(
                objectError.getDefaultMessage(),
                objectError.getCode()
        );
    }

    private static FieldCodeValidationProblemDetails handleFieldError(@NonNull FieldError fieldError) {
        return new FieldCodeValidationProblemDetails(
                fieldError.getDefaultMessage(),
                fieldError.getField(),
                fieldError.getCode()
        );
    }

}

package me.stinper.jwtauth.exception.handler;

import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.RequestParameterProblem;
import me.stinper.jwtauth.core.error.RequestComponentErrorCode;
import me.stinper.jwtauth.exception.NoSuchPropertyException;
import me.stinper.jwtauth.exception.response.UnresolvedPropertyErrorResponse;
import me.stinper.jwtauth.utils.MessageSourceHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

/**
 * Служит для обработки исключений, связанных с любыми компонентами запроса, такими как
 * Query params (те, что через ?), заголовки и т.д.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class RequestComponentsExceptionHandler {
    private final MessageSourceHelper messageSourceHelper;

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RequestParameterProblem> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new RequestParameterProblem(
                                RequestComponentErrorCode.REQUIRED_HEADER_MISSED.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                        "messages.request-components.headers.required-header-missed",
                                        e.getHeaderName()
                                ),
                                e.getHeaderName()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RequestParameterProblem> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new RequestParameterProblem(
                                RequestComponentErrorCode.REQUEST_PARAMETER_TYPE_MISMATCH.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                        "messages.request-components.commons.type-mismatch",
                                        e.getName()
                                ),
                                e.getName()
                        )
                );
    }

    @ExceptionHandler(NoSuchPropertyException.class)
    public ResponseEntity<UnresolvedPropertyErrorResponse> handleNoSuchPropertyException(NoSuchPropertyException e) {
        String propertyName = e.getPropertyReferenceException().getPropertyName();
        Set<String> propertyMatches = e.getPropertyMatches();

        String propertyHints = null;

        if (!propertyMatches.isEmpty())
            propertyHints = messageSourceHelper.getLocalizedMessage(
                    "messages.request-components.query-params.unresolved-property-hint",
                    StringUtils.collectionToDelimitedString(propertyMatches, ",", "'", "'")
            );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new UnresolvedPropertyErrorResponse(
                                RequestComponentErrorCode.UNRESOLVED_PROPERTY.getCode(),
                                messageSourceHelper.getLocalizedMessage(
                                        "messages.request-components.query-params.unresolved-property",
                                        propertyName
                                ),
                                propertyName,
                                propertyHints
                        )
                );
    }
}

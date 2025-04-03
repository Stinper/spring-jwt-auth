package me.stinper.commons.api.response.beanvalidation;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.validation.RequestBodyValidationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.provider.registry.ConstraintAnnotationErrorResponseProvidersRegistry;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractConstraintViolationsErrorResponseBuilder {
    protected final ConstraintAnnotationErrorResponseProvidersRegistry errorResponseProvidersRegistry;
    protected final PropertyPathExtractor propertyPathExtractor;

    public abstract List<RequestBodyValidationProblemDetails> buildErrorResponseDetailsFromConstraintViolations(
            Collection<ConstraintViolation<?>> constraintViolations);
}

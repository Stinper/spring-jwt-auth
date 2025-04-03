package me.stinper.commons.api.response.beanvalidation;

import jakarta.validation.ConstraintViolation;
import me.stinper.commons.api.response.validation.RequestBodyValidationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.provider.ConstraintAnnotationErrorResponseProvider;
import me.stinper.commons.api.response.beanvalidation.provider.registry.ConstraintAnnotationErrorResponseProvidersRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DefaultConstraintViolationsErrorResponseBuilder extends AbstractConstraintViolationsErrorResponseBuilder {

    public DefaultConstraintViolationsErrorResponseBuilder(
            ConstraintAnnotationErrorResponseProvidersRegistry errorResponseProvidersRegistry,
            PropertyPathExtractor propertyPathExtractor) {
        super(errorResponseProvidersRegistry, propertyPathExtractor);
    }

    @Override
    public List<RequestBodyValidationProblemDetails> buildErrorResponseDetailsFromConstraintViolations(
            Collection<ConstraintViolation<?>> constraintViolations) {

        List<RequestBodyValidationProblemDetails> errorResponseDetails = new ArrayList<>(constraintViolations.size());

        for (ConstraintViolation<?> cv : constraintViolations) {
            ConstraintAnnotationErrorResponseProvider errorResponseProvider =
                    this.errorResponseProvidersRegistry.getResponseProviderFor(cv);

            errorResponseDetails.add(
                    errorResponseProvider.buildErrorResponseDetails(cv, this.propertyPathExtractor)
            );
        }

        return errorResponseDetails;
    }
}

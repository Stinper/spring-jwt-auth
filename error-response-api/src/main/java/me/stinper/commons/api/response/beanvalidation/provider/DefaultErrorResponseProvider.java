package me.stinper.commons.api.response.beanvalidation.provider;

import jakarta.validation.ConstraintViolation;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationDetails;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.enums.ConstraintViolationType;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;

import java.lang.annotation.Annotation;


public record DefaultErrorResponseProvider(
        ConstraintViolationType constraintViolationType

) implements ConstraintAnnotationErrorResponseProvider {

    public static DefaultErrorResponseProvider withUnknownConstraintViolationType() {
        return new DefaultErrorResponseProvider(ConstraintViolationType.UNKNOWN);
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return true; // Дефолтный провайдер поддерживает все виды аннотаций, потому что он их не использует
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                ConstraintViolationDetails.fromType(this.constraintViolationType)
        );
    }
}

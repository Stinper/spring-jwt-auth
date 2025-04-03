package me.stinper.jwtauth.validation.constraints;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationProblemDetails;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.provider.ConstraintAnnotationErrorResponseProvider;

import java.lang.annotation.Annotation;

@RequiredArgsConstructor
public class PasswordConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Password.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new PasswordConstraintViolationDetails(passwordPolicyProperties)
        );
    }
}

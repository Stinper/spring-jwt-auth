package me.stinper.jwtauth.config;

import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.beanvalidation.AbstractConstraintViolationsErrorResponseBuilder;
import me.stinper.commons.api.response.beanvalidation.DefaultConstraintViolationsErrorResponseBuilder;
import me.stinper.commons.api.response.beanvalidation.path.PropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.path.DottedPropertyPathExtractor;
import me.stinper.commons.api.response.beanvalidation.provider.registry.ConstraintAnnotationErrorResponseProvidersRegistry;
import me.stinper.commons.api.response.beanvalidation.provider.registry.DefaultConstraintAnnotationErrorResponseProvidersRegistry;
import me.stinper.jwtauth.validation.constraints.Password;
import me.stinper.jwtauth.validation.constraints.PasswordConstraintErrorResponseProvider;
import me.stinper.jwtauth.validation.constraints.PasswordPolicyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ResponseApiBeans {
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Bean
    public PropertyPathExtractor propertyPathExtractor() {
        return new DottedPropertyPathExtractor();
    }

    @Bean
    public ConstraintAnnotationErrorResponseProvidersRegistry errorResponseProvidersRegistry() {
        ConstraintAnnotationErrorResponseProvidersRegistry registry = new DefaultConstraintAnnotationErrorResponseProvidersRegistry();

        registry.registerProvider(Password.class, new PasswordConstraintErrorResponseProvider(passwordPolicyProperties));

        return registry;
    }

    @Bean
    public AbstractConstraintViolationsErrorResponseBuilder errorResponseBuilder() {
        return new DefaultConstraintViolationsErrorResponseBuilder(this.errorResponseProvidersRegistry(), this.propertyPathExtractor());
    }

}

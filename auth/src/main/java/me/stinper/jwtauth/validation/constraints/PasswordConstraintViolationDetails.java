package me.stinper.jwtauth.validation.constraints;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.beanvalidation.ConstraintViolationDetails;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PasswordConstraintViolationDetails extends ConstraintViolationDetails {
    public static final String CONSTRAINT_VIOLATION_TYPE = "PASSWORD";

    @JsonProperty(value = "min_length")
    private final int minLength;

    @JsonProperty(value = "min_letters_count")
    private final int minLettersCount;

    @JsonProperty(value = "min_upper_letters_count")
    private final int minUpperLettersCount;

    public PasswordConstraintViolationDetails(PasswordPolicyProperties passwordPolicyProperties) {
        super(CONSTRAINT_VIOLATION_TYPE);
        this.minLength = passwordPolicyProperties.getMinLength();
        this.minLettersCount = passwordPolicyProperties.getMinLettersCount();
        this.minUpperLettersCount = passwordPolicyProperties.getMinUpperLettersCount();
    }
}

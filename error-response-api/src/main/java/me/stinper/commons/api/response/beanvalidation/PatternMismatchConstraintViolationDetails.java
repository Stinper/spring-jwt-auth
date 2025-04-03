package me.stinper.commons.api.response.beanvalidation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.beanvalidation.enums.ConstraintViolationType;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PatternMismatchConstraintViolationDetails extends ConstraintViolationDetails {
    private final String pattern;

    public PatternMismatchConstraintViolationDetails(String pattern) {
        super(ConstraintViolationType.PATTERN_MISMATCH);
        this.pattern = pattern;
    }
}

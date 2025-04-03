package me.stinper.commons.api.response.beanvalidation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.stinper.commons.api.response.beanvalidation.enums.ConstraintViolationType;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MinMaxConstraintViolationDetails<T, S> extends ConstraintViolationDetails {
    private final T min;

    private final S max;
    public MinMaxConstraintViolationDetails(T min, S max) {
        super(ConstraintViolationType.MIN_MAX);
        this.min = min;
        this.max = max;
    }
}

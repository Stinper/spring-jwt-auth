package me.stinper.commons.api.response.beanvalidation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.stinper.commons.api.response.beanvalidation.enums.ConstraintViolationType;

@Data
@RequiredArgsConstructor
public class ConstraintViolationDetails {

    @JsonProperty(value = "constraint_violation_type")
    private final String constraintViolationType;

    public ConstraintViolationDetails(ConstraintViolationType type) {
        this.constraintViolationType = type.getType();
    }

    public static ConstraintViolationDetails fromType(ConstraintViolationType type) {
        return new ConstraintViolationDetails(type.getType());
    }

    public static ConstraintViolationDetails fromType(String type) {
        return new ConstraintViolationDetails(type);
    }
}

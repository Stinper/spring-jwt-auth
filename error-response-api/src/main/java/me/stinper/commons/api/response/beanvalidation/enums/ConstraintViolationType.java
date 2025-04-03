package me.stinper.commons.api.response.beanvalidation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Тип ограничения, которое было нарушено
 */
@RequiredArgsConstructor
@Getter
public enum ConstraintViolationType {
    /**
     * Нарушено ограничения типа NotNull
     * @see jakarta.validation.constraints.NotNull
     */
    NOT_NULL("not_null"),

    /**
     * Нарушено ограничение типа NotBlank
     * @see jakarta.validation.constraints.NotBlank
     */
    NOT_BLANK("not_blank"),

    /**
     * Нарушено ограничение, связанное с соответствием определенному паттерну
     * @see jakarta.validation.constraints.Email
     * @see jakarta.validation.constraints.Pattern
     */
    PATTERN_MISMATCH("pattern_mismatch"),

    /**
     * Нарушено ограничение, связанное с диапазоном значений. К этому типу относятся
     * как диапазон [Min, Max], так и одиночные значения Min или Max
     * @see jakarta.validation.constraints.Size
     * @see jakarta.validation.constraints.Max
     * @see jakarta.validation.constraints.Min
     */
    MIN_MAX("min_max"),

    /**
     * @see jakarta.validation.constraints.Positive
     */
    POSITIVE("positive"),

    /**
     * @see jakarta.validation.constraints.PositiveOrZero
     */
    POSITIVE_OR_ZERO("positive_or_zero"),

    /**
     * @see jakarta.validation.constraints.Negative
     */
    NEGATIVE("negative"),

    /**
     * @see jakarta.validation.constraints.NegativeOrZero
     */
    NEGATIVE_OR_ZERO("negative_or_zero"),

    /**
     * Нарушено неизвестное ограничение, которое не поддерживается ни одним из провайдеров
     */
    UNKNOWN("unknown");

    /**
     * Текстовое представление типа нарушенного ограничения
     */
    private final String type;
}
